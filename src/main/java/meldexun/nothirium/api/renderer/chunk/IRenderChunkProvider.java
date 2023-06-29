package meldexun.nothirium.api.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.util.Direction;

public interface IRenderChunkProvider<T extends IRenderChunk> {

	void init(int renderDistanceX, int renderDistanceY, int renderDistanceZ);

	void repositionCamera(double cameraX, double cameraY, double cameraZ);

	void setDirty(int chunkX, int chunkY, int chunkZ);

	@Nullable
	T getRenderChunkAt(int chunkX, int chunkY, int chunkZ);

	@Nullable
	T getNeighbor(T renderChunk, Direction direction);

	void setNeighbor(T renderChunk, Direction direction, @Nullable T neighbor);

	void releaseBuffers();

}
