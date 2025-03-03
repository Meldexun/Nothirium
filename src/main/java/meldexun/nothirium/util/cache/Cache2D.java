package meldexun.nothirium.util.cache;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.apache.commons.lang3.Validate;

import meldexun.nothirium.util.function.IntInt2ObjFunction;
import meldexun.nothirium.util.function.IntIntObj2ObjFunction;
import net.minecraft.util.math.BlockPos;

public class Cache2D<V> {

	private final int startX;
	private final int startZ;
	private final int endX;
	private final int endZ;
	private final int sizeX;
	private final int sizeZ;
	private final V defaultValue;
	private final V[] data;

	public Cache2D(int startX, int startZ, int endX, int endZ, V defaultValue, IntFunction<V[]> init) {
		this.startX = startX;
		this.startZ = startZ;
		this.endX = endX;
		this.endZ = endZ;
		this.sizeX = endX - startX + 1;
		this.sizeZ = endZ - startZ + 1;
		this.defaultValue = defaultValue;
		this.data = init.apply(sizeX * sizeZ);
	}

	public Cache2D(int startX, int startZ, int endX, int endZ, V defaultValue, V[] data) {
		this.startX = startX;
		this.startZ = startZ;
		this.endX = endX;
		this.endZ = endZ;
		this.sizeX = endX - startX + 1;
		this.sizeZ = endZ - startZ + 1;
		this.defaultValue = defaultValue;
		this.data = data;
		Validate.isTrue(sizeX * sizeZ == data.length);
	}

	private int index(BlockPos pos) {
		return index(pos.getX(), pos.getZ());
	}

	private int index(int x, int z) {
		return (x - startX) * sizeZ + z - startZ;
	}

	public boolean inBounds(BlockPos pos) {
		return inBounds(pos.getX(), pos.getZ());
	}

	public boolean inBounds(int x, int z) {
		if (x < startX || x > endX) {
			return false;
		}
		return z >= startZ && z <= endZ;
	}

	public void put(BlockPos pos, V v) {
		put(pos.getX(), pos.getZ(), v);
	}

	public void put(int x, int z, V v) {
		if (!inBounds(x, z)) {
			return;
		}
		data[index(x, z)] = v;
	}

	public V get(BlockPos pos) {
		return get(pos.getX(), pos.getZ());
	}

	public V get(int x, int z) {
		if (!inBounds(x, z)) {
			return this.defaultValue;
		}
		return data[index(x, z)];
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

	public V computeIfAbsent(int x, int z, IntInt2ObjFunction<V> mappingFunction) {
		if (!inBounds(x, z)) {
			return this.defaultValue;
		}
		int index = index(x, z);
		V v = data[index];
		if (v == null) {
			v = mappingFunction.apply(x, z);
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

	public V computeIfPresent(int x, int z, IntIntObj2ObjFunction<V, V> mappingFunction) {
		if (!inBounds(x, z)) {
			return this.defaultValue;
		}
		int index = index(x, z);
		V v = data[index];
		if (v != null) {
			v = mappingFunction.apply(x, z, v);
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

	public V compute(int x, int z, IntIntObj2ObjFunction<V, V> mappingFunction) {
		if (!inBounds(x, z)) {
			return this.defaultValue;
		}
		int index = index(x, z);
		V v = mappingFunction.apply(x, z, data[index]);
		data[index] = v;
		return v;
	}

	public V[] getData() {
		return data;
	}

}
