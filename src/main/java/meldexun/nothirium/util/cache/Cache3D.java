package meldexun.nothirium.util.cache;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.apache.commons.lang3.Validate;

import meldexun.nothirium.util.function.IntIntInt2ObjFunction;
import meldexun.nothirium.util.function.IntIntIntObj2ObjFunction;
import net.minecraft.util.math.BlockPos;

public class Cache3D<V> {

	private final int startX;
	private final int startY;
	private final int startZ;
	private final int endX;
	private final int endY;
	private final int endZ;
	private final int sizeX;
	private final int sizeY;
	private final int sizeZ;
	private final V defaultValue;
	private final V[] data;

	public Cache3D(int startX, int startY, int startZ, int endX, int endY, int endZ, V defaultValue,
			IntFunction<V[]> init) {
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

	public Cache3D(int startX, int startY, int startZ, int endX, int endY, int endZ, V defaultValue, V[] data) {
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
		this.data = data;
		Validate.isTrue(sizeX * sizeY * sizeZ == data.length);
	}

	private int index(BlockPos pos) {
		return index(pos.getX(), pos.getY(), pos.getZ());
	}

	private int index(int x, int y, int z) {
		return ((x - startX) * sizeY + y - startY) * sizeZ + z - startZ;
	}

	public boolean inBounds(BlockPos pos) {
		return inBounds(pos.getX(), pos.getY(), pos.getZ());
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

	public void put(BlockPos pos, V v) {
		put(pos.getX(), pos.getY(), pos.getZ(), v);
	}

	public void put(int x, int y, int z, V v) {
		if (!inBounds(x, y, z)) {
			return;
		}
		data[index(x, y, z)] = v;
	}

	public V get(BlockPos pos) {
		return get(pos.getX(), pos.getY(), pos.getZ());
	}

	public V get(int x, int y, int z) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		return data[index(x, y, z)];
	}

	public V computeIfAbsent(BlockPos pos, Function<BlockPos, V> mappingFunction) {
		if (!inBounds(pos)) {
			return this.defaultValue;
		}
		int index = index(pos);
		V v = data[index];
		if (v == null) {
			v = mappingFunction.apply(pos);
			data[index] = v;
		}
		return v;
	}

	public V computeIfAbsent(int x, int y, int z, IntIntInt2ObjFunction<V> mappingFunction) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		int index = index(x, y, z);
		V v = data[index];
		if (v == null) {
			v = mappingFunction.apply(x, y, z);
			data[index] = v;
		}
		return v;
	}

	public V computeIfPresent(BlockPos pos, BiFunction<BlockPos, V, V> mappingFunction) {
		if (!inBounds(pos)) {
			return this.defaultValue;
		}
		int index = index(pos);
		V v = data[index];
		if (v != null) {
			v = mappingFunction.apply(pos, v);
			data[index] = v;
		}
		return v;
	}

	public V computeIfPresent(int x, int y, int z, IntIntIntObj2ObjFunction<V, V> mappingFunction) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		int index = index(x, y, z);
		V v = data[index];
		if (v != null) {
			v = mappingFunction.apply(x, y, z, v);
			data[index] = v;
		}
		return v;
	}

	public V compute(BlockPos pos, BiFunction<BlockPos, V, V> mappingFunction) {
		if (!inBounds(pos)) {
			return this.defaultValue;
		}
		int index = index(pos);
		V v = mappingFunction.apply(pos, data[index]);
		data[index] = v;
		return v;
	}

	public V compute(int x, int y, int z, IntIntIntObj2ObjFunction<V, V> mappingFunction) {
		if (!inBounds(x, y, z)) {
			return this.defaultValue;
		}
		int index = index(x, y, z);
		V v = mappingFunction.apply(x, y, z, data[index]);
		data[index] = v;
		return v;
	}

	public V[] getData() {
		return data;
	}

}
