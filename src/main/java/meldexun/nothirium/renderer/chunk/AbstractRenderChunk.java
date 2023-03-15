package meldexun.nothirium.renderer.chunk;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkTask;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.Frustum;

public abstract class AbstractRenderChunk<N extends AbstractRenderChunk<N>> implements IRenderChunk<N> {

	private static final int ALL_NEIGHBORS_LOADED;
	static {
		int i = 1 << Direction.ALL.length;
		for (Direction direction : Direction.HORIZONTAL)
			i |= 1 << direction.ordinal();
		ALL_NEIGHBORS_LOADED = i;
	}
	private int x;
	private int y;
	private int z;
	@SuppressWarnings("unchecked")
	private final AbstractRenderChunk<N>[] neighbors = new AbstractRenderChunk[Direction.ALL.length];
	private int neighborsLoaded;
	private boolean isLoaded;
	public int lastTimeEnqueued = -1;
	public int lastTimeRecorded = -1;
	private VisibilitySet visibilitySet = new VisibilitySet();
	public int visibleDirections;
	private boolean dirty;
	private IRenderChunkTask lastCompileTask;
	private CompletableFuture<RenderChunkTaskResult> lastCompileTaskResult;
	private final Enum2ObjMap<ChunkRenderPass, IVBOPart> vboParts = new Enum2ObjMap<>(ChunkRenderPass.class);
	private ByteBuffer translucentVertexData;
	private int nonemptyVboParts;

	protected AbstractRenderChunk(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.markDirty();
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getZ() {
		return this.z;
	}

	@Override
	public boolean setCoords(int x, int y, int z) {
		if (this.x != x || this.y != y || this.z != z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.releaseBuffers();
			this.markDirty();
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public N getNeighbor(Direction direction) {
		return (N) this.neighbors[direction.ordinal()];
	}

	@Override
	public void setNeighbor(Direction direction, @Nullable N neighbor) {
		this.neighbors[direction.ordinal()] = neighbor;

		if (direction != Direction.DOWN && direction != Direction.UP)
			if (neighbor == null || neighbor.isLoaded()) {
				neighborsLoaded |= 1 << direction.ordinal();
			} else {
				neighborsLoaded &= ~(1 << direction.ordinal());
			}
	}

	public boolean hasAllNeighbors() {
		return neighborsLoaded == ALL_NEIGHBORS_LOADED;
	}

	public boolean isLoaded() {
		return this.isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;

		if (isLoaded) {
			neighborsLoaded |= 1 << Direction.ALL.length;
			for (Direction direction : Direction.HORIZONTAL) {
				AbstractRenderChunk<N> neighbor = neighbors[direction.ordinal()];
				if (neighbor != null)
					neighbor.neighborsLoaded |= 1 << direction.opposite().ordinal();
			}
		} else {
			neighborsLoaded &= ~(1 << Direction.ALL.length);
			for (Direction direction : Direction.HORIZONTAL) {
				AbstractRenderChunk<N> neighbor = neighbors[direction.ordinal()];
				if (neighbor != null)
					neighbor.neighborsLoaded &= ~(1 << direction.opposite().ordinal());
			}
		}
	}

	public VisibilitySet getVisibility() {
		return visibilitySet;
	}

	public void setVisibility(VisibilitySet visibilitySet) {
		this.visibilitySet = visibilitySet;
	}

	public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) {
		double x = MathUtil.clamp(cameraX, this.x, this.x + 16) - cameraX;
		double y = MathUtil.clamp(cameraY, this.y, this.y + 16) - cameraY;
		double z = MathUtil.clamp(cameraZ, this.z, this.z + 16) - cameraZ;
		return Math.max(x * x + z * z, y * y) > fogEndSqr;
	}

	public boolean isFrustumCulled(Frustum frustum) {
		return !frustum.isAABBInFrustum(this.x, this.y, this.z, this.x + 16, this.y + 16, this.z + 16);
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
			this.translucentVertexData = null;
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
	}

	protected boolean canCompile() {
		return this.isDirty() && this.hasAllNeighbors();
	}

	public void compileAsync(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		if (!this.canCompile()) {
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
	public ByteBuffer getTranslucentVertexData() {
		return translucentVertexData;
	}

	public void setTranslucentVertexData(@Nullable ByteBuffer translucentVertexData) {
		this.translucentVertexData = translucentVertexData;
	}

}
