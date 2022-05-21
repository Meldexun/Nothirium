package meldexun.nothirium.util.collection;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Enum2IntMap<E extends Enum<E>> implements IEnumMap<E, Integer> {

	private final E[] enums;
	private final int[] values;

	public Enum2IntMap(Class<E> enumClass) {
		this.enums = enumClass.getEnumConstants();
		this.values = new int[enums.length];
	}

	public Enum2IntMap(Class<E> enumClass, ToIntFunction<E> mapper) {
		this(enumClass);
		this.fill(mapper);
	}

	public Enum2IntMap(Class<E> enumClass, IntSupplier mapper) {
		this(enumClass);
		this.fill(mapper);
	}

	@Deprecated
	@Override
	public Integer get(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void set(E e, Integer t) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public Stream<Integer> stream() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void forEach(BiConsumer<E, Integer> action) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void forEach(Consumer<Integer> action) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void fill(Function<E, Integer> mapper) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public void fill(Supplier<Integer> mapper) {
		throw new UnsupportedOperationException();
	}

	public int getInt(E e) {
		return values[e.ordinal()];
	}

	public void set(E e, int t) {
		values[e.ordinal()] = t;
	}

	public void or(E e, int t) {
		values[e.ordinal()] |= t;
	}

	public void and(E e, int t) {
		values[e.ordinal()] &= t;
	}

	public IntStream streamInt() {
		return Arrays.stream(values);
	}

	public void forEach(ObjIntConsumer<E> action) {
		for (E e : enums) {
			action.accept(e, values[e.ordinal()]);
		}
	}

	public void forEach(IntConsumer action) {
		for (int t : values) {
			action.accept(t);
		}
	}

	public void fill(ToIntFunction<E> mapper) {
		for (E e : enums) {
			values[e.ordinal()] = mapper.applyAsInt(e);
		}
	}

	public void fill(IntSupplier mapper) {
		for (E e : enums) {
			values[e.ordinal()] = mapper.getAsInt();
		}
	}

}
