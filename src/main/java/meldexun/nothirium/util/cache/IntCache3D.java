package meldexun.nothirium.util.cache;

import java.util.function.IntFunction;

import meldexun.nothirium.util.function.IntIntIntInt2IntFunction;
import meldexun.nothirium.util.function.ObjInt2IntFunction;
import net.minecraft.util.math.BlockPos;

public class IntCache3D {

	private final int startX;
	private final int startY;
	private final int startZ;
	private final int endX;
	private final int endY;
	private final int endZ;
	private final int sizeX;
	private final int sizeY;
	private final int sizeZ;
	private final int defaultValue;
	private final int[] data;

	public IntCache3D(int startX, int startY, int startZ, int endX, int endY, int endZ, int defaultValue,
			IntFunction<int[]> init) {
		this.startX = startX;
		this.startY = startY;
		this.startZ = startZ;
		this.endX = endX;
		this.endY = endY;
		this.endZ = endZ;
		this.sizeX = endX - startX + 1;
		this.sizeY = endY - startY + 1;
		this.sizeZ = endZ - startZ + 1;
		this.defaultValue = defaultValue;
		this.data = init.apply(sizeX * sizeY * sizeZ);
	}

	private int index(int x, int y, int z) {
		return ((x - startX) * sizeY + y - startY) * sizeZ + z - startZ;
	}

	public boolean inBounds(int x, int y, int z) {
		if (x < startX || x > endX) {
			return false;
		}
		if (y < startY || y > endY) {
			return false;
		}
		return z >= startZ && z <= endZ;
	}

	public void put(BlockPos pos, int v) {
		put(pos.getX(), pos.getY(), pos.getZ(), v);
	}

	public void put(int x, int y, int z, int v) {
		if (!inBounds(x, y, z)) {
			return;
		}
		data[index(x, y, z)] = v;
	}

	public int get(BlockPos pos) {
		return get(pos.getX(), pos.getY(), pos.getZ());
	}

	public int get(int x, int y, int z) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		return data[index(x, y, z)];
	}

	public int compute(BlockPos pos, ObjInt2IntFunction<BlockPos> mappingFunction) {
		return compute(pos.getX(), pos.getY(), pos.getZ(), (x, y, z, v) -> mappingFunction.apply(pos, v));
	}

	public int compute(int x, int y, int z, IntIntIntInt2IntFunction mappingFunction) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		int index = index(x, y, z);
		int v = mappingFunction.apply(x, y, z, data[index]);
		data[index] = v;
		return v;
	}

}
