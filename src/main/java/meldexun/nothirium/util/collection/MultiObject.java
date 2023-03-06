package meldexun.nothirium.util.collection;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import meldexun.nothirium.util.function.IntObjConsumer;

public class MultiObject<T> implements IMultiObject<T> {

	protected final int size;
	protected final Object[] values;
	protected int index;
	private T value;

	public MultiObject(int size) {
		this.size = size;
		this.values = new Object[size];
	}

	public MultiObject(int size, IntFunction<T> mapper) {
		this(size);
		this.fill(mapper);
	}

	public MultiObject(int size, Supplier<T> mapper) {
		this(size);
		this.fill(mapper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		index = (index + 1) % size;
		value = (T) values[index];
	}

	@Override
	public T get() {
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		return (T) values[index];
	}

	@Override
	public void set(T t) {
		values[index] = t;
		value = t;
	}

	@Override
	public void set(int index, T t) {
		values[index] = t;
		if (index == this.index) {
			value = t;
		}
	}

	@Override
	public Stream<T> stream() {
		return StreamSupport.stream(Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forEach(IntObjConsumer<T> action) {
		for (int i = 0; i < values.length; i++) {
			action.accept(i, (T) values[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forEach(Consumer<T> action) {
		for (Object t : values) {
			action.accept((T) t);
		}
	}

	@Override
	public void fill(IntFunction<T> mapper) {
		for (int i = 0; i < values.length; i++) {
			values[i] = mapper.apply(i);
		}
	}

	@Override
	public void fill(Supplier<T> mapper) {
		for (int i = 0; i < values.length; i++) {
			values[i] = mapper.get();
		}
	}

}
