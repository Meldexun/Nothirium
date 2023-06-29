package meldexun.nothirium.renderer.chunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.mc.util.FogUtil;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.Frustum;

public abstract class AbstractChunkRenderer<T extends AbstractRenderChunk> implements IChunkRenderer<T> {

	private final Queue<T> chunkQueue = new ArrayDeque<>();
	private double lastTransparencyResortX;
	private double lastTransparencyResortY;
	private double lastTransparencyResortZ;
	private int renderedChunks;
	protected final Enum2ObjMap<ChunkRenderPass, List<T>> chunks = new Enum2ObjMap<>(ChunkRenderPass.class, (Supplier<List<T>>) ArrayList::new);

	protected AbstractChunkRenderer() {

	}

	@Override
	public int renderedChunks() {
		return renderedChunks;
	}

	@Override
	public int renderedChunks(ChunkRenderPass pass) {
		return chunks.get(pass).size();
	}

	@Override
	public void setup(IRenderChunkProvider<T> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		this.resortTransparency(cameraX, cameraY, cameraZ, frustum);

		int chunkX = MathUtil.floor(cameraX) >> 4;
		int chunkY = MathUtil.floor(cameraY) >> 4;
		int chunkZ = MathUtil.floor(cameraZ) >> 4;

		renderedChunks = 0;
		chunks.forEach(List::clear);
		T rootRenderChunk = renderChunkProvider.getRenderChunkAt(chunkX, chunkY, chunkZ);
		rootRenderChunk.visibleDirections = 0x3F;
		chunkQueue.add(rootRenderChunk);

		double fogEndSqr = FogUtil.calculateFogEndSqr();
		boolean spectator = isSpectator();

		T renderChunk;
		while ((renderChunk = chunkQueue.poll()) != null) {
			renderChunk.lastTimeRecorded = frame;
			renderChunk.compileAsync(this, ChunkRenderManager.getTaskDispatcher());
			addToRenderLists(renderChunk);

			for (Direction direction : Direction.ALL) {
				T neighbor = renderChunkProvider.getNeighbor(renderChunk, direction);
				if (neighbor == null)
					continue;
				if (neighbor.lastTimeRecorded == frame)
					continue;
				if (direction.opposite().isFaceCulled(neighbor, cameraX, cameraY, cameraZ))
					continue;
				if (!spectator && !renderChunk.isVisibleFromAnyOrigin(direction))
					continue;
				if (neighbor.lastTimeEnqueued != frame) {
					neighbor.lastTimeEnqueued = frame;
					if (neighbor.isFogCulled(cameraX, cameraY, cameraZ, fogEndSqr) || neighbor.isFrustumCulled(frustum)) {
						neighbor.lastTimeRecorded = frame;
						continue;
					}
					neighbor.resetOrigins();
					chunkQueue.add(neighbor);
				}
				neighbor.setOrigin(direction.opposite());
			}
		}
	}

	private void resortTransparency(double cameraX, double cameraY, double cameraZ, Frustum frustum) {
		double dx = cameraX - lastTransparencyResortX;
		double dy = cameraY - lastTransparencyResortY;
		double dz = cameraZ - lastTransparencyResortZ;
		if (dx * dx + dy * dy + dz * dz > 1.0D) {
			lastTransparencyResortX = cameraX;
			lastTransparencyResortY = cameraY;
			lastTransparencyResortZ = cameraZ;

			IRenderChunkProvider<T> provider = ChunkRenderManager.getProvider();
			IRenderChunkDispatcher taskDispatcher = ChunkRenderManager.getTaskDispatcher();
			int chunkX = MathUtil.floor(cameraX) >> 4;
			int chunkY = MathUtil.floor(cameraY) >> 4;
			int chunkZ = MathUtil.floor(cameraZ) >> 4;
			int r = 2;
			for (int x = -r; x <= r; x++) {
				for (int y = -r; y <= r; y++) {
					for (int z = -r; z <= r; z++) {
						T renderChunk = provider.getRenderChunkAt(chunkX + x, chunkY + y, chunkZ + z);
						if (renderChunk == null)
							continue;
						if (renderChunk.isFrustumCulled(frustum))
							continue;
						renderChunk.resortTransparency(this, taskDispatcher);
					}
				}
			}
		}
	}

	private void addToRenderLists(T renderChunk) {
		if (renderChunk.isEmpty()) {
			return;
		}
		renderedChunks++;
		chunks.forEach((pass, list) -> {
			if (renderChunk.getVBOPart(pass) != null) {
				list.add(renderChunk);
			}
		});
	}

	protected abstract boolean isSpectator();

}
