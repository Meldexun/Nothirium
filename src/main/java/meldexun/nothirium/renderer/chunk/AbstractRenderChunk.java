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
import meldexun.nothirium.util.Axis;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.nothirium.util.function.NullableObjIntIntIntPredicate;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.Frustum;

public abstract class AbstractRenderChunk<N extends AbstractRenderChunk<N>> implements IRenderChunk<N> {

	private int x;
	private int y;
	private int z;
	@SuppressWarnings("unchecked")
	private final AbstractRenderChunk<N>[] neighbors = new AbstractRenderChunk[Direction.ALL.length];
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
	}

	public boolean isLoaded() {
		return this.isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
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
		return this.isDirty() && this.isLoaded() && this.allNeighborsLoaded();
	}

	protected boolean allNeighborsLoaded() {
		return neighborsMatch(this, this.getX() >> 4, this.getY() >> 4, this.getZ() >> 4, Axis.X, Axis.Z, Axis.Y, this::isNeighborLoaded);
	}

	protected abstract boolean isNeighborLoaded(@Nullable N neighbor, int chunkX, int chunkY, int chunkZ);

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Axis axis, NullableObjIntIntIntPredicate<N> predicate) {
		return neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis.getNegative(), predicate)
				&& neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis.getPositive(), predicate);
	}

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Axis axis, Axis axis1, NullableObjIntIntIntPredicate<N> predicate) {
		return neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis, predicate)
				&& neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis1.getNegative(), axis, predicate)
				&& neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis1.getPositive(), axis, predicate);
	}

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Axis axis, Axis axis1, Axis axis2, NullableObjIntIntIntPredicate<N> predicate) {
		return neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis, axis1, predicate)
				&& neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis2.getNegative(), axis, axis1, predicate)
				&& neighborsMatch(renderChunk, chunkX, chunkY, chunkZ, axis2.getPositive(), axis, axis1, predicate);
	}

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Direction direction, NullableObjIntIntIntPredicate<N> predicate) {
		N neighbor = renderChunk != null ? renderChunk.getNeighbor(direction) : null;
		int x1 = chunkX + direction.getX();
		int y1 = chunkY + direction.getY();
		int z1 = chunkZ + direction.getZ();
		if (!predicate.test(neighbor, x1, y1, z1))
			return false;
		return true;
	}

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Direction direction, Axis axis1, NullableObjIntIntIntPredicate<N> predicate) {
		N neighbor = renderChunk != null ? renderChunk.getNeighbor(direction) : null;
		int x1 = chunkX + direction.getX();
		int y1 = chunkY + direction.getY();
		int z1 = chunkZ + direction.getZ();
		if (!predicate.test(neighbor, x1, y1, z1))
			return false;
		if (!neighborsMatch(neighbor, x1, y1, z1, axis1, predicate))
			return false;
		return true;
	}

	public static <N extends AbstractRenderChunk<N>> boolean neighborsMatch(@Nullable AbstractRenderChunk<N> renderChunk, int chunkX, int chunkY, int chunkZ, Direction direction, Axis axis1, Axis axis2, NullableObjIntIntIntPredicate<N> predicate) {
		N neighbor = renderChunk != null ? renderChunk.getNeighbor(direction) : null;
		int x1 = chunkX + direction.getX();
		int y1 = chunkY + direction.getY();
		int z1 = chunkZ + direction.getZ();
		if (!predicate.test(neighbor, x1, y1, z1))
			return false;
		if (!neighborsMatch(neighbor, x1, y1, z1, axis1, predicate))
			return false;
		if (!neighborsMatch(neighbor, x1, y1, z1, axis2, axis1, predicate))
			return false;
		return true;
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
