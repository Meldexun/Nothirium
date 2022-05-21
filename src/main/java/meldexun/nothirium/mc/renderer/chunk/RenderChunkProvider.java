package meldexun.nothirium.mc.renderer.chunk;

import meldexun.nothirium.renderer.chunk.AbstractRenderChunkProvider;
import net.minecraft.client.Minecraft;

public class RenderChunkProvider extends AbstractRenderChunkProvider<RenderChunk> {

	private boolean chunkCached;
	private int cachedChunkX;
	private int cachedChunkZ;
	private boolean cachedChunkLoaded;

	@Override
	public void init(int renderDistance) {
		super.init(renderDistance);
		chunkCached = false;
	}

	@Override
	public void repositionCamera(double cameraX, double cameraY, double cameraZ) {
		super.repositionCamera(cameraX, cameraY, cameraZ);
		chunkCached = false;
	}

	@Override
	protected boolean isChunkLoaded(int chunkX, int chunkY, int chunkZ) {
		if (!chunkCached || chunkX != cachedChunkX || chunkZ != cachedChunkZ) {
			Minecraft mc = Minecraft.getMinecraft();
			cachedChunkLoaded = mc.world != null && mc.world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) != null;
			cachedChunkX = chunkX;
			cachedChunkZ = chunkZ;
			chunkCached = true;
		}
		return cachedChunkLoaded;
	}

	@Override
	protected RenderChunk createRenderChunk(int x, int y, int z) {
		return new RenderChunk(x, y, z);
	}

	public void setLoaded(int chunkX, int chunkZ, boolean isLoaded) {
		int y1 = cameraChunkY - gridSizeY / 2;
		int y2 = cameraChunkY + gridSizeY / 2;
		for (int chunkY = y1; chunkY <= y2; chunkY++) {
			RenderChunk renderChunk = getRenderChunkAt(chunkX, chunkY, chunkZ);
			if (renderChunk != null)
				renderChunk.setLoaded(isLoaded);
		}
	}

}
