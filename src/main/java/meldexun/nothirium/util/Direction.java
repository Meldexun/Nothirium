package meldexun.nothirium.util;

import meldexun.nothirium.api.renderer.chunk.IRenderChunk;

public enum Direction {

	DOWN(0, Axis.Y, 0, -1, 0) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			// TODO move this into the mc package
			if (renderChunk.getSectionY() >= 16)
				return true;
			return cameraY > renderChunk.getY();
		}
	},
	UP(1, Axis.Y, 0, 1, 0) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			// TODO move this into the mc package
			if (renderChunk.getSectionY() < 0)
				return true;
			return cameraY < renderChunk.getY() + 16;
		}
	},
	NORTH(2, Axis.Z, 0, 0, -1) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			return cameraZ > renderChunk.getZ();
		}
	},
	SOUTH(3, Axis.Z, 0, 0, 1) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			return cameraZ < renderChunk.getZ() + 16;
		}
	},
	WEST(4, Axis.X, -1, 0, 0) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			return cameraX > renderChunk.getX();
		}
	},
	EAST(5, Axis.X, 1, 0, 0) {
		@Override
		public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
			return cameraX < renderChunk.getX() + 16;
		}
	};

	static {
		WEST.opposite = EAST;
		EAST.opposite = WEST;
		DOWN.opposite = UP;
		UP.opposite = DOWN;
		NORTH.opposite = SOUTH;
		SOUTH.opposite = NORTH;

		Axis.X.positive = EAST;
		Axis.X.negative = WEST;
		Axis.Y.positive = UP;
		Axis.Y.negative = DOWN;
		Axis.Z.positive = SOUTH;
		Axis.Z.negative = NORTH;
	}

	public static final Direction[] ALL = Direction.values();
	public static final Direction[] HORIZONTAL = {
			NORTH,
			SOUTH,
			WEST,
			EAST };
	public static final Direction[] VERTICAL = {
			DOWN,
			UP };

	private final int index;
	private final Axis axis;
	private Direction opposite;
	private final int x;
	private final int y;
	private final int z;

	private Direction(int index, Axis axis, int x, int y, int z) {
		this.index = index;
		this.axis = axis;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getIndex() {
		return this.index;
	}

	public Direction opposite() {
		return this.opposite;
	}

	public Axis getAxis() {
		return axis;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public static Direction get(int index) {
		return ALL[index];
	}

	public static Direction get(float x, float y, float z) {
		float x1 = Math.abs(x);
		float y1 = Math.abs(y);
		float z1 = Math.abs(z);
		if (x1 >= y1 && x1 >= z1) {
			return x < 0.0F ? WEST : EAST;
		} else if (y1 >= z1) {
			return y < 0.0F ? DOWN : UP;
		} else {
			return z < 0.0F ? NORTH : SOUTH;
		}
	}

	public abstract boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ);

}
