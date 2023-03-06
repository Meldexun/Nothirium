package meldexun.nothirium.util.collection;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import meldexun.nothirium.util.function.IntBiConsumer;
import meldexun.nothirium.util.function.IntObjConsumer;

public class IntMultiObject implements IMultiObject<Integer> {

	protected final int size;
	protected final int[] values;
	protected int index;
	private int value;

	public IntMultiObject(int size) {
		this.size = size;
		this.values = new int[size];
	}

	public IntMultiObject(int size, IntUnaryOperator mapper) {
		this(size);
		this.fill(mapper);
	}

	public IntMultiObject(int size, IntSupplier mapper) {
		this(size);
		this.fill(mapper);
	}

	@Override
	public void update() {
		index = (index + 1) % size;
		value = values[index];
	}

	@Deprecated
	@Override
	public Integer get() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public Integer get(int index) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void set(Integer t) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void set(int index, Integer t) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public Stream<Integer> stream() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void forEach(IntObjConsumer<Integer> action) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void forEach(Consumer<Integer> action) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void fill(IntFunction<Integer> mapper) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void fill(Supplier<Integer> mapper) {
		throw new UnsupportedOperationException();
	}

	public int getInt() {
		return value;
	}

	public int getInt(int index) {
		return values[index];
	}

	public void set(int t) {
		values[index] = t;
		value = t;
	}

	public void set(int index, int t) {
		values[index] = t;
		if (index == this.index) {
			value = t;
		}
	}

	public IntStream streamInt() {
		return Arrays.stream(values);
	}

	public void forEach(IntBiConsumer action) {
		for (int i = 0; i < values.length; i++) {
			action.accept(i, values[i]);
		}
	}

	public void forEach(IntConsumer action) {
		for (int t : values) {
			action.accept(t);
		}
	}

	public void fill(IntUnaryOperator mapper) {
		for (int i = 0; i < values.length; i++) {
			values[i] = mapper.applyAsInt(i);
		}
	}

	public void fill(IntSupplier mapper) {
		for (int i = 0; i < values.length; i++) {
			values[i] = mapper.getAsInt();
		}
	}

}
