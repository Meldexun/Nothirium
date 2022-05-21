package meldexun.nothirium.util.collection;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Enum2ObjMap<E extends Enum<E>, T> implements IEnumMap<E, T> {

	private final E[] enums;
	private final Object[] values;

	public Enum2ObjMap(Class<E> enumClass) {
		this.enums = enumClass.getEnumConstants();
		this.values = new Object[enums.length];
	}

	public Enum2ObjMap(Class<E> enumClass, Function<E, T> mapper) {
		this(enumClass);
		this.fill(mapper);
	}

	public Enum2ObjMap(Class<E> enumClass, Supplier<T> mapper) {
		this(enumClass);
		this.fill(mapper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(E e) {
		return (T) values[e.ordinal()];
	}

	@Override
	public void set(E e, T t) {
		values[e.ordinal()] = t;
	}

	@Override
	public Stream<T> stream() {
		return StreamSupport.stream(Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forEach(BiConsumer<E, T> action) {
		for (E e : enums) {
			action.accept(e, (T) values[e.ordinal()]);
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
	public void fill(Function<E, T> mapper) {
		for (E e : enums) {
			values[e.ordinal()] = mapper.apply(e);
		}
	}

	@Override
	public void fill(Supplier<T> mapper) {
		for (E e : enums) {
			values[e.ordinal()] = mapper.get();
		}
	}

}
