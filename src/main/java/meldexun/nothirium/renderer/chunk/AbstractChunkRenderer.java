package meldexun.nothirium.renderer.chunk;

import java.util.ArrayDeque;
import java.util.Queue;

import org.lwjgl.opengl.GL11;

import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.Frustum;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public abstract class AbstractChunkRenderer<T extends AbstractRenderChunk<T>> implements IChunkRenderer<T> {

	private final Queue<T> chunkQueue = new ArrayDeque<T>(1024);
	private double lastTransparencyResortX;
	private double lastTransparencyResortY;
	private double lastTransparencyResortZ;

	protected AbstractChunkRenderer() {

	}

	@Override
	public void setup(IRenderChunkProvider<T> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		int chunkX = MathUtil.floor(cameraX) >> 4;
		int chunkY = MathUtil.floor(cameraY) >> 4;
		int chunkZ = MathUtil.floor(cameraZ) >> 4;

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		double dx = entity.posX - lastTransparencyResortX;
		double dy = entity.posY - lastTransparencyResortY;
		double dz = entity.posZ - lastTransparencyResortZ;
		if (dx * dx + dy * dy + dz * dz > 1.0D) {
			lastTransparencyResortX = entity.posX;
			lastTransparencyResortY = entity.posY;
			lastTransparencyResortZ = entity.posZ;
			int r = 2;
			for (int x = -r; x <= r; x++) {
				for (int y = -r; y <= r; y++) {
					for (int z = -r; z <= r; z++) {
						T renderChunk = renderChunkProvider.getRenderChunkAt(chunkX + x, chunkY + y, chunkZ + z);
						if (renderChunk.isFrustumCulled(frustum))
							continue;
						renderChunk.resortTransparency(this, ChunkRenderManager.getTaskDispatcher());
					}
				}
			}
		}

		T rootRenderChunk = renderChunkProvider.getRenderChunkAt(chunkX, chunkY, chunkZ);
		rootRenderChunk.visibleDirections = 0x3F;
		chunkQueue.add(rootRenderChunk);

		double fogEnd = GL11.glGetFloat(GL11.GL_FOG_END);

		T renderChunk;
		while ((renderChunk = chunkQueue.poll()) != null) {
			renderChunk.lastTimeRecorded = frame;
			renderChunk.compileAsync(this, ChunkRenderManager.getTaskDispatcher());
			record(renderChunk, cameraX, cameraY, cameraZ);

			for (Direction direction : Direction.ALL) {
				T neighbor = renderChunk.getNeighbor(direction);
				if (neighbor == null)
					continue;
				if (neighbor.lastTimeRecorded == frame)
					continue;
				if (neighbor.isFaceCulled(cameraX, cameraY, cameraZ, direction.opposite()))
					continue;
				if (!renderChunk.isVisibleFromAnyOrigin(direction))
					continue;
				if (neighbor.lastTimeEnqueued != frame) {
					neighbor.lastTimeEnqueued = frame;
					if (neighbor.isFogCulled(cameraX, cameraY, cameraZ, fogEnd) || neighbor.isFrustumCulled(frustum)) {
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

	protected abstract void record(T renderChunk, double cameraX, double cameraY, double cameraZ);

}
