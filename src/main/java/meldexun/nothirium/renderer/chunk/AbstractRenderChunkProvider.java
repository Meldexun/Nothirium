package meldexun.nothirium.renderer.chunk;

import java.util.Arrays;

import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.function.IntIntInt2ObjFunction;
import meldexun.nothirium.util.function.ObjIntIntIntConsumer;
import meldexun.nothirium.util.function.ObjObjObjObjConsumer;
import meldexun.nothirium.util.math.MathUtil;

public abstract class AbstractRenderChunkProvider<T extends AbstractRenderChunk> implements IRenderChunkProvider<T> {

	protected int gridSizeX;
	protected int gridSizeY;
	protected int gridSizeZ;
	protected int cameraChunkX;
	protected int cameraChunkY;
	protected int cameraChunkZ;
	protected AbstractRenderChunk[] chunks;

	@Override
	public void init(int renderDistanceX, int renderDistanceY, int renderDistanceZ) {
		this.gridSizeX = renderDistanceX * 2 + 1;
		this.gridSizeY = renderDistanceY * 2 + 1;
		this.gridSizeZ = renderDistanceZ * 2 + 1;
		this.cameraChunkX = renderDistanceX;
		this.cameraChunkY = renderDistanceY;
		this.cameraChunkZ = renderDistanceZ;
		this.chunks = new AbstractRenderChunk[this.gridSizeX * this.gridSizeY * this.gridSizeZ];

		for (int x = 0; x < this.gridSizeX; x++) {
			for (int z = 0; z < this.gridSizeZ; z++) {
				for (int y = 0; y < this.gridSizeY; y++) {
					T renderChunk = this.createRenderChunk(x, y, z);
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

	protected abstract T createRenderChunk(int x, int y, int z);

	@Override
	public void repositionCamera(double cameraX, double cameraY, double cameraZ) {
		int newCameraChunkX = MathUtil.floor(cameraX) >> 4;
		int newCameraChunkY = MathUtil.floor(cameraY) >> 4;
		int newCameraChunkZ = MathUtil.floor(cameraZ) >> 4;

		if (MathUtil.floorMod(newCameraChunkX, this.gridSizeX) != MathUtil.floorMod(this.cameraChunkX, this.gridSizeX)) {
			updateNeighborRelations(newCameraChunkX, this.cameraChunkX, this.gridSizeX, this.gridSizeY, this.gridSizeZ, this::getXYZ, this::updateNeighborX);
		}
		if (MathUtil.floorMod(newCameraChunkY, this.gridSizeY) != MathUtil.floorMod(this.cameraChunkY, this.gridSizeY)) {
			updateNeighborRelations(newCameraChunkY, this.cameraChunkY, this.gridSizeY, this.gridSizeX, this.gridSizeZ, this::getYXZ, this::updateNeighborY);
		}
		if (MathUtil.floorMod(newCameraChunkZ, this.gridSizeZ) != MathUtil.floorMod(this.cameraChunkZ, this.gridSizeZ)) {
			updateNeighborRelations(newCameraChunkZ, this.cameraChunkZ, this.gridSizeZ, this.gridSizeX, this.gridSizeY, this::getZXY, this::updateNeighborZ);
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
			updateRenderChunkPositions(newCameraChunkY, newCameraChunkX, newCameraChunkZ, this.gridSizeY, this.gridSizeX, this.gridSizeZ, this::getYXZ, this::updatePositionYXZ);
		} else {
			if (newCameraChunkX != this.cameraChunkX) {
				updateRenderChunkPositions(newCameraChunkX, newCameraChunkY, newCameraChunkZ, this.cameraChunkX, this.gridSizeX, this.gridSizeY, this.gridSizeZ, this::getXYZ, this::updatePositionXYZ);
			}
			if (newCameraChunkY != this.cameraChunkY) {
				updateRenderChunkPositions(newCameraChunkY, newCameraChunkX, newCameraChunkZ, this.cameraChunkY, this.gridSizeY, this.gridSizeX, this.gridSizeZ, this::getYXZ, this::updatePositionYXZ);
			}
			if (newCameraChunkZ != this.cameraChunkZ) {
				updateRenderChunkPositions(newCameraChunkZ, newCameraChunkX, newCameraChunkY, this.cameraChunkZ, this.gridSizeZ, this.gridSizeX, this.gridSizeY, this::getZXY, this::updatePositionZXY);
			}
		}

		this.cameraChunkX = newCameraChunkX;
		this.cameraChunkY = newCameraChunkY;
		this.cameraChunkZ = newCameraChunkZ;
	}

	private static <T> void updateNeighborRelations(int newX, int oldX, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjObjObjObjConsumer<T, T, T, T> neighborUpdateFunc) {
		int r = sizeX >> 1;
		int oldMinX = MathUtil.floorMod(oldX - r, sizeX);
		int oldMaxX = MathUtil.floorMod(oldX + r, sizeX);
		int newMinX = MathUtil.floorMod(newX - r, sizeX);
		int newMaxX = MathUtil.floorMod(newX + r, sizeX);
		for (int y = 0; y < sizeY; y++) {
			for (int z = 0; z < sizeZ; z++) {
				T c1 = renderChunkFunc.apply(oldMinX, y, z);
				T c2 = renderChunkFunc.apply(oldMaxX, y, z);
				T c3 = renderChunkFunc.apply(newMinX, y, z);
				T c4 = renderChunkFunc.apply(newMaxX, y, z);
				neighborUpdateFunc.accept(c1, c2, c3, c4);
			}
		}
	}

	private T getXYZ(int x, int y, int z) {
		return this.getRenderChunkAtUnchecked(x, y, z);
	}

	private T getYXZ(int y, int x, int z) {
		return this.getRenderChunkAtUnchecked(x, y, z);
	}

	private T getZXY(int z, int x, int y) {
		return this.getRenderChunkAtUnchecked(x, y, z);
	}

	private void updateNeighborX(T c1, T c2, T c3, T c4) {
		c1.setNeighbor(Direction.WEST, c2);
		c2.setNeighbor(Direction.EAST, c1);
		c3.setNeighbor(Direction.WEST, null);
		c4.setNeighbor(Direction.EAST, null);
	}

	private void updateNeighborY(T c1, T c2, T c3, T c4) {
		c1.setNeighbor(Direction.DOWN, c2);
		c2.setNeighbor(Direction.UP, c1);
		c3.setNeighbor(Direction.DOWN, null);
		c4.setNeighbor(Direction.UP, null);
	}

	private void updateNeighborZ(T c1, T c2, T c3, T c4) {
		c1.setNeighbor(Direction.NORTH, c2);
		c2.setNeighbor(Direction.SOUTH, c1);
		c3.setNeighbor(Direction.NORTH, null);
		c4.setNeighbor(Direction.SOUTH, null);
	}

	private static <T> void updateRenderChunkPositions(int x0, int x1, int y0, int y1, int z0, int z1, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
		for (int x = x0; x <= x1; x++) {
			int ix = MathUtil.floorMod(x, sizeX);

			for (int y = y0; y <= y1; y++) {
				int iy = MathUtil.floorMod(y, sizeY);

				for (int z = z0; z <= z1; z++) {
					int iz = MathUtil.floorMod(z, sizeZ);

					positionUpdateFunc.accept(renderChunkFunc.apply(ix, iy, iz), x, y, z);
				}
			}
		}
	}

	private static <T> void updateRenderChunkPositions(int newX, int newY, int newZ, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
		int rx = sizeX >> 1;
		int ry = sizeY >> 1;
		int rz = sizeZ >> 1;
		int x0 = newX - rx;
		int x1 = newX + rx;
		int y0 = newY - ry;
		int y1 = newY + ry;
		int z0 = newZ - rz;
		int z1 = newZ + rz;

		updateRenderChunkPositions(x0, x1, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
	}

	private static <T> void updateRenderChunkPositions(int newX, int newY, int newZ, int oldX, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
		int rx = sizeX >> 1;
		int ry = sizeY >> 1;
		int rz = sizeZ >> 1;
		int y0 = newY - ry;
		int y1 = newY + ry;
		int z0 = newZ - rz;
		int z1 = newZ + rz;

		if (oldX < newX) {
			updateRenderChunkPositions(oldX + rx + 1, newX + rx, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
		} else {
			updateRenderChunkPositions(newX - rx, oldX - rx - 1, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
		}
	}

	private void updatePositionXYZ(T renderChunk, int x, int y, int z) {
		renderChunk.setCoords(x, y, z);
	}

	private void updatePositionYXZ(T renderChunk, int y, int x, int z) {
		renderChunk.setCoords(x, y, z);
	}

	private void updatePositionZXY(T renderChunk, int z, int x, int y) {
		renderChunk.setCoords(x, y, z);
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

	@SuppressWarnings("unchecked")
	@Override
	public T getNeighbor(T renderChunk, Direction direction) {
		return (T) renderChunk.getNeighbor(direction);
	}

	@Override
	public void setNeighbor(T renderChunk, Direction direction, T neighbor) {
		renderChunk.setNeighbor(direction, neighbor);
	}

	@Override
	public void releaseBuffers() {
		Arrays.stream(this.chunks).forEach(AbstractRenderChunk::releaseBuffers);
	}

}
