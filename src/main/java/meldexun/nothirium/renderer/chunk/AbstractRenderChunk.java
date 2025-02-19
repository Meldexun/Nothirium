package meldexun.nothirium.renderer.chunk;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import meldexun.memoryutil.UnsafeByteBuffer;
import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkTask;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.SectionPos;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.Frustum;

public abstract class AbstractRenderChunk implements IRenderChunk {

	private SectionPos pos;
	private final AbstractRenderChunk[] neighbors = new AbstractRenderChunk[Direction.ALL.length];
	public int lastTimeEnqueued = -1;
	public int lastTimeRecorded = -1;
	private VisibilitySet visibilitySet = new VisibilitySet();
	public int visibleDirections;
	private boolean dirty;
	private IRenderChunkTask lastCompileTask;
	private CompletableFuture<RenderChunkTaskResult> lastCompileTaskResult;
	private final Enum2ObjMap<ChunkRenderPass, IVBOPart> vboParts = new Enum2ObjMap<>(ChunkRenderPass.class);
	private UnsafeByteBuffer translucentVertexData;
	private int nonemptyVboParts;

	protected AbstractRenderChunk(int sectionX, int sectionY, int sectionZ) {
		this.pos = SectionPos.of(sectionX, sectionY, sectionZ);
		this.markDirty();
	}

	@Override
	public SectionPos getPos() {
		return this.pos;
	}

	@Override
	public boolean setCoords(int sectionX, int sectionY, int sectionZ) {
		if (this.getSectionX() != sectionX || this.getSectionY() != sectionY || this.getSectionZ() != sectionZ) {
			this.pos = SectionPos.of(sectionX, sectionY, sectionZ);
			this.releaseBuffers();
			this.markDirty();
			return true;
		}
		return false;
	}

	@Nullable
	AbstractRenderChunk getNeighbor(Direction direction) {
		return this.neighbors[direction.ordinal()];
	}

	void setNeighbor(Direction direction, @Nullable AbstractRenderChunk neighbor) {
		this.neighbors[direction.ordinal()] = neighbor;
	}

	public VisibilitySet getVisibility() {
		return visibilitySet;
	}

	public void setVisibility(VisibilitySet visibilitySet) {
		this.visibilitySet = visibilitySet;
	}

	public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) {
		// TODO support other fog shapes
		double x = MathUtil.clamp(cameraX, this.getX(), this.getX() + 16) - cameraX;
		double y = MathUtil.clamp(cameraY, this.getY(), this.getY() + 16) - cameraY;
		double z = MathUtil.clamp(cameraZ, this.getZ(), this.getZ() + 16) - cameraZ;
		return Math.max(x * x + z * z, y * y) > fogEndSqr;
	}

	public boolean isFrustumCulled(Frustum frustum) {
		return !frustum.isAABBInFrustum(this.getX(), this.getY(), this.getZ(), this.getX() + 16, this.getY() + 16, this.getZ() + 16);
	}

	public boolean isVisibleFromAnyOrigin(Direction direction) {
		return (visibleDirections & (1 << direction.ordinal())) != 0;
	}

	public void setOrigin(Direction origin) {
		visibleDirections |= getVisibility().allVisibleFrom(origin);
	}

	public void resetOrigins() {
		this.visibleDirections = 0;
	}

	public void markDirty() {
		this.dirty = true;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	@Override
	@Nullable
	public IVBOPart getVBOPart(ChunkRenderPass pass) {
		return this.vboParts.get(pass);
	}

	@Override
	public void setVBOPart(ChunkRenderPass pass, @Nullable IVBOPart vboPart) {
		if (this.vboParts.get(pass) != null) {
			this.vboParts.get(pass).free();
		}
		this.vboParts.set(pass, vboPart);
		if (vboPart != null) {
			nonemptyVboParts |= 1 << pass.ordinal();
		} else {
			nonemptyVboParts &= ~(1 << pass.ordinal());
		}
		if (pass == ChunkRenderPass.TRANSLUCENT) {
			this.setTranslucentVertexData(null);
		}
	}

	@Override
	public boolean isEmpty() {
		return nonemptyVboParts == 0;
	}

	public void cancelTask() {
		if (this.lastCompileTask != null) {
			this.lastCompileTask.cancel();
			this.lastCompileTask = null;
			this.lastCompileTaskResult = null;
		}
	}

	public void releaseBuffers() {
		this.cancelTask();
		Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> this.setVBOPart(pass, null));
		this.setTranslucentVertexData(null);
	}

	protected abstract boolean canCompile();

	public void compileAsync(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		if (!this.isDirty() || !this.canCompile()) {
			return;
		}
		this.cancelTask();
		this.dirty = false;
		this.lastCompileTask = this.createCompileTask(chunkRenderer, taskDispatcher);
		if (this.lastCompileTask != null) {
			lastCompileTaskResult = taskDispatcher.runAsync(this.lastCompileTask);
		} else {
			Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> this.setVBOPart(pass, null));
			this.visibilitySet.setAllVisible();
		}
	}

	@Nullable
	protected abstract AbstractRenderChunkTask<?> createCompileTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher);

	public void resortTransparency(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		if (this.isDirty())
			return;
		if (lastCompileTaskResult != null && !lastCompileTaskResult.isDone())
			return;
		this.lastCompileTask = this.createSortTranslucentTask(chunkRenderer, taskDispatcher);
		if (this.lastCompileTask != null) {
			lastCompileTaskResult = taskDispatcher.runAsync(this.lastCompileTask);
		}
	}

	@Nullable
	protected abstract AbstractRenderChunkTask<?> createSortTranslucentTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher);

	@Nullable
	public UnsafeByteBuffer getTranslucentVertexData() {
		return translucentVertexData;
	}

	public void setTranslucentVertexData(@Nullable UnsafeByteBuffer translucentVertexData) {
		if (this.translucentVertexData != null)
			this.translucentVertexData.close();
		this.translucentVertexData = translucentVertexData;
	}

}
