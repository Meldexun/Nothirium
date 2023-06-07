package meldexun.nothirium.api.renderer.chunk;

import javax.annotation.Nullable;

public interface IRenderChunkProvider<T extends IRenderChunk<T>> {

	void init(int renderDistanceX, int renderDistanceY, int renderDistanceZ);

	void repositionCamera(double cameraX, double cameraY, double cameraZ);

	void setDirty(int chunkX, int chunkY, int chunkZ);

	@Nullable
	T getRenderChunkAt(int chunkX, int chunkY, int chunkZ);

	void releaseBuffers();

}
