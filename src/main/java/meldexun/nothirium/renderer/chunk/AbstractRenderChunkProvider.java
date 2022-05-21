package meldexun.nothirium.renderer.chunk;

import java.util.Arrays;

import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.math.MathUtil;

public abstract class AbstractRenderChunkProvider<T extends AbstractRenderChunk<T>> implements IRenderChunkProvider<T> {

	protected int gridSizeX;
	protected int gridSizeY;
	protected int gridSizeZ;
	protected int cameraChunkX;
	protected int cameraChunkY;
	protected int cameraChunkZ;
	protected AbstractRenderChunk<T>[] chunks;

	@SuppressWarnings("unchecked")
	@Override
	public void init(int renderDistance) {
		this.gridSizeX = renderDistance * 2 + 1;
		this.gridSizeY = renderDistance * 2 + 1;
		this.gridSizeZ = renderDistance * 2 + 1;
		this.cameraChunkX = renderDistance;
		this.cameraChunkY = renderDistance;
		this.cameraChunkZ = renderDistance;
		this.chunks = new AbstractRenderChunk[this.gridSizeX * this.gridSizeY * this.gridSizeZ];

		for (int x = 0; x < this.gridSizeX; x++) {
			for (int z = 0; z < this.gridSizeZ; z++) {
				for (int y = 0; y < this.gridSizeY; y++) {
					T renderChunk = this.createRenderChunk(x << 4, y << 4, z << 4);
					renderChunk.setLoaded(this.isChunkLoaded(x, y, z));
					this.chunks[this.getChunkIndex(x, y, z)] = renderChunk;

					if (x > 0) {
						T neighbor = this.getRenderChunkAtUnchecked(x - 1, y, z);
						renderChunk.setNeighbor(Direction.WEST, neighbor);
						neighbor.setNeighbor(Direction.EAST, renderChunk);
					}
					if (y > 0) {
						T neighbor = this.getRenderChunkAtUnchecked(x, y - 1, z);
						renderChunk.setNeighbor(Direction.DOWN, neighbor);
						neighbor.setNeighbor(Direction.UP, renderChunk);
					}
					if (z > 0) {
						T neighbor = this.getRenderChunkAtUnchecked(x, y, z - 1);
						renderChunk.setNeighbor(Direction.NORTH, neighbor);
						neighbor.setNeighbor(Direction.SOUTH, renderChunk);
					}
				}
			}
		}
	}

	private int getChunkIndex(int chunkX, int chunkY, int chunkZ) {
		return (chunkZ * this.gridSizeY + chunkY) * this.gridSizeX + chunkX;
	}

	protected abstract boolean isChunkLoaded(int chunkX, int chunkY, int chunkZ);

	protected abstract T createRenderChunk(int x, int y, int z);

	@Override
	public void repositionCamera(double cameraX, double cameraY, double cameraZ) {
		int newCameraChunkX = MathUtil.floor(cameraX) >> 4;
		int newCameraChunkY = MathUtil.floor(cameraY) >> 4;
		int newCameraChunkZ = MathUtil.floor(cameraZ) >> 4;

		if (MathUtil.floorMod(newCameraChunkX, this.gridSizeX) != MathUtil.floorMod(this.cameraChunkX, this.gridSizeX)) {
			int oldMinChunkX = MathUtil.floorMod(this.cameraChunkX - this.gridSizeX / 2, this.gridSizeX);
			int oldMaxChunkX = MathUtil.floorMod(this.cameraChunkX + this.gridSizeX / 2, this.gridSizeX);
			int newMinChunkX = MathUtil.floorMod(newCameraChunkX - this.gridSizeX / 2, this.gridSizeX);
			int newMaxChunkX = MathUtil.floorMod(newCameraChunkX + this.gridSizeX / 2, this.gridSizeX);

			for (int y = 0; y < this.gridSizeY; y++) {
				for (int z = 0; z < this.gridSizeZ; z++) {
					T renderChunk1 = this.getRenderChunkAtUnchecked(oldMinChunkX, y, z);
					T renderChunk2 = this.getRenderChunkAtUnchecked(oldMaxChunkX, y, z);
					T renderChunk3 = this.getRenderChunkAtUnchecked(newMinChunkX, y, z);
					T renderChunk4 = this.getRenderChunkAtUnchecked(newMaxChunkX, y, z);
					renderChunk1.setNeighbor(Direction.WEST, renderChunk2);
					renderChunk2.setNeighbor(Direction.EAST, renderChunk1);
					renderChunk3.setNeighbor(Direction.WEST, null);
					renderChunk4.setNeighbor(Direction.EAST, null);
				}
			}
		}
		if (MathUtil.floorMod(newCameraChunkY, this.gridSizeY) != MathUtil.floorMod(this.cameraChunkY, this.gridSizeY)) {
			int oldMinChunkY = MathUtil.floorMod(this.cameraChunkY - this.gridSizeY / 2, this.gridSizeY);
			int oldMaxChunkY = MathUtil.floorMod(this.cameraChunkY + this.gridSizeY / 2, this.gridSizeY);
			int newMinChunkY = MathUtil.floorMod(newCameraChunkY - this.gridSizeY / 2, this.gridSizeY);
			int newMaxChunkY = MathUtil.floorMod(newCameraChunkY + this.gridSizeY / 2, this.gridSizeY);

			for (int x = 0; x < this.gridSizeX; x++) {
				for (int z = 0; z < this.gridSizeZ; z++) {
					T renderChunk1 = this.getRenderChunkAtUnchecked(x, oldMinChunkY, z);
					T renderChunk2 = this.getRenderChunkAtUnchecked(x, oldMaxChunkY, z);
					T renderChunk3 = this.getRenderChunkAtUnchecked(x, newMinChunkY, z);
					T renderChunk4 = this.getRenderChunkAtUnchecked(x, newMaxChunkY, z);
					renderChunk1.setNeighbor(Direction.DOWN, renderChunk2);
					renderChunk2.setNeighbor(Direction.UP, renderChunk1);
					renderChunk3.setNeighbor(Direction.DOWN, null);
					renderChunk4.setNeighbor(Direction.UP, null);
				}
			}
		}
		if (MathUtil.floorMod(newCameraChunkZ, this.gridSizeZ) != MathUtil.floorMod(this.cameraChunkZ, this.gridSizeZ)) {
			int oldMinChunkZ = MathUtil.floorMod(this.cameraChunkZ - this.gridSizeZ / 2, this.gridSizeZ);
			int oldMaxChunkZ = MathUtil.floorMod(this.cameraChunkZ + this.gridSizeZ / 2, this.gridSizeZ);
			int newMinChunkZ = MathUtil.floorMod(newCameraChunkZ - this.gridSizeZ / 2, this.gridSizeZ);
			int newMaxChunkZ = MathUtil.floorMod(newCameraChunkZ + this.gridSizeZ / 2, this.gridSizeZ);

			for (int x = 0; x < this.gridSizeX; x++) {
				for (int y = 0; y < this.gridSizeY; y++) {
					T renderChunk1 = this.getRenderChunkAtUnchecked(x, y, oldMinChunkZ);
					T renderChunk2 = this.getRenderChunkAtUnchecked(x, y, oldMaxChunkZ);
					T renderChunk3 = this.getRenderChunkAtUnchecked(x, y, newMinChunkZ);
					T renderChunk4 = this.getRenderChunkAtUnchecked(x, y, newMaxChunkZ);
					renderChunk1.setNeighbor(Direction.NORTH, renderChunk2);
					renderChunk2.setNeighbor(Direction.SOUTH, renderChunk1);
					renderChunk3.setNeighbor(Direction.NORTH, null);
					renderChunk4.setNeighbor(Direction.SOUTH, null);
				}
			}
		}

		int threshold = this.gridSizeX * this.gridSizeY * this.gridSizeZ;
		int offX = Math.abs(newCameraChunkX - this.cameraChunkX);
		int offY = Math.abs(newCameraChunkY - this.cameraChunkY);
		int offZ = Math.abs(newCameraChunkZ - this.cameraChunkZ);
		long updX = (long) offX * this.gridSizeY * this.gridSizeZ;
		long updY = (long) this.gridSizeX * offY * this.gridSizeZ;
		long updZ = (long) this.gridSizeX * this.gridSizeY * offZ;
		if (updX + updY + updZ >= threshold) {
			// update all
			for (int x = newCameraChunkX - this.gridSizeX / 2; x <= newCameraChunkX + this.gridSizeX / 2; x++) {
				int x1 = MathUtil.floorMod(x, this.gridSizeX);

				for (int z = newCameraChunkZ - this.gridSizeZ / 2; z <= newCameraChunkZ + this.gridSizeZ / 2; z++) {
					int z1 = MathUtil.floorMod(z, this.gridSizeZ);

					for (int y = newCameraChunkY - this.gridSizeY / 2; y <= newCameraChunkY + this.gridSizeY / 2; y++) {
						int y1 = MathUtil.floorMod(y, this.gridSizeY);

						T renderChunk = this.getRenderChunkAtUnchecked(x1, y1, z1);
						renderChunk.setCoords(x << 4, y << 4, z << 4);
						renderChunk.setLoaded(this.isChunkLoaded(x, y, z));
					}
				}
			}
		} else {
			if (newCameraChunkX != this.cameraChunkX) {
				int start;
				int end;
				int step;
				if (newCameraChunkX < this.cameraChunkX) {
					start = newCameraChunkX - this.gridSizeX / 2;
					end = this.cameraChunkX - this.gridSizeX / 2;
					step = 1;
				} else {
					start = newCameraChunkX + this.gridSizeX / 2;
					end = this.cameraChunkX + this.gridSizeX / 2;
					step = -1;
				}

				for (int x = start; x != end; x += step) {
					int x1 = MathUtil.floorMod(x, this.gridSizeX);

					for (int z = newCameraChunkZ - this.gridSizeZ / 2; z <= newCameraChunkZ + this.gridSizeZ / 2; z++) {
						int z1 = MathUtil.floorMod(z, this.gridSizeZ);

						for (int y = newCameraChunkY - this.gridSizeY / 2; y <= newCameraChunkY + this.gridSizeY / 2; y++) {
							int y1 = MathUtil.floorMod(y, this.gridSizeY);

							T renderChunk = this.getRenderChunkAtUnchecked(x1, y1, z1);
							renderChunk.setCoords(x << 4, y << 4, z << 4);
							renderChunk.setLoaded(this.isChunkLoaded(x, y, z));
						}
					}
				}
			}
			if (newCameraChunkY != this.cameraChunkY) {
				int start;
				int end;
				int step;
				if (newCameraChunkY < this.cameraChunkY) {
					start = newCameraChunkY - this.gridSizeY / 2;
					end = this.cameraChunkY - this.gridSizeY / 2;
					step = 1;
				} else {
					start = newCameraChunkY + this.gridSizeY / 2;
					end = this.cameraChunkY + this.gridSizeY / 2;
					step = -1;
				}

				for (int x = newCameraChunkX - this.gridSizeX / 2; x <= newCameraChunkX + this.gridSizeX / 2; x++) {
					int x1 = MathUtil.floorMod(x, this.gridSizeX);

					for (int z = newCameraChunkZ - this.gridSizeZ / 2; z <= newCameraChunkZ + this.gridSizeZ / 2; z++) {
						int z1 = MathUtil.floorMod(z, this.gridSizeZ);

						for (int y = start; y != end; y += step) {
							int y1 = MathUtil.floorMod(y, this.gridSizeY);

							T renderChunk = this.getRenderChunkAtUnchecked(x1, y1, z1);
							renderChunk.setCoords(x << 4, y << 4, z << 4);
							renderChunk.setLoaded(this.isChunkLoaded(x, y, z));
						}
					}
				}
			}
			if (newCameraChunkZ != this.cameraChunkZ) {
				int start;
				int end;
				int step;
				if (newCameraChunkZ < this.cameraChunkZ) {
					start = newCameraChunkZ - this.gridSizeZ / 2;
					end = this.cameraChunkZ - this.gridSizeZ / 2;
					step = 1;
				} else {
					start = newCameraChunkZ + this.gridSizeZ / 2;
					end = this.cameraChunkZ + this.gridSizeZ / 2;
					step = -1;
				}

				for (int x = newCameraChunkX - this.gridSizeX / 2; x <= newCameraChunkX + this.gridSizeX / 2; x++) {
					int x1 = MathUtil.floorMod(x, this.gridSizeX);

					for (int z = start; z != end; z += step) {
						int z1 = MathUtil.floorMod(z, this.gridSizeZ);

						for (int y = newCameraChunkY - this.gridSizeY / 2; y <= newCameraChunkY + this.gridSizeY / 2; y++) {
							int y1 = MathUtil.floorMod(y, this.gridSizeY);

							T renderChunk = this.getRenderChunkAtUnchecked(x1, y1, z1);
							renderChunk.setCoords(x << 4, y << 4, z << 4);
							renderChunk.setLoaded(this.isChunkLoaded(x, y, z));
						}
					}
				}
			}
		}

		this.cameraChunkX = newCameraChunkX;
		this.cameraChunkY = newCameraChunkY;
		this.cameraChunkZ = newCameraChunkZ;
	}

	@Override
	public void setDirty(int chunkX, int chunkY, int chunkZ) {
		T renderChunk = this.getRenderChunkAt(chunkX, chunkY, chunkZ);
		if (renderChunk != null) {
			renderChunk.markDirty();
		}
	}

	@Override
	public T getRenderChunkAt(int chunkX, int chunkY, int chunkZ) {
		if (chunkX < this.cameraChunkX - this.gridSizeX / 2) {
			return null;
		}
		if (chunkX > this.cameraChunkX + this.gridSizeX / 2) {
			return null;
		}
		if (chunkY < this.cameraChunkY - this.gridSizeY / 2) {
			return null;
		}
		if (chunkY > this.cameraChunkY + this.gridSizeY / 2) {
			return null;
		}
		if (chunkZ < this.cameraChunkZ - this.gridSizeZ / 2) {
			return null;
		}
		if (chunkZ > this.cameraChunkZ + this.gridSizeZ / 2) {
			return null;
		}
		chunkX = MathUtil.floorMod(chunkX, this.gridSizeX);
		chunkY = MathUtil.floorMod(chunkY, this.gridSizeY);
		chunkZ = MathUtil.floorMod(chunkZ, this.gridSizeZ);
		return this.getRenderChunkAtUnchecked(chunkX, chunkY, chunkZ);
	}

	@SuppressWarnings("unchecked")
	private T getRenderChunkAtUnchecked(int chunkX, int chunkY, int chunkZ) {
		return (T) this.chunks[this.getChunkIndex(chunkX, chunkY, chunkZ)];
	}

	@Override
	public void releaseBuffers() {
		Arrays.stream(this.chunks).forEach(AbstractRenderChunk::releaseBuffers);
	}

}
