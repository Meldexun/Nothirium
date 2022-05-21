package meldexun.nothirium.util.collection;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import meldexun.nothirium.util.function.IntObjConsumer;

public interface IMultiObject<T> {

	void update();

	T get();

	T get(int index);

	void set(T t);

	void set(int index, T t);

	Stream<T> stream();

	void forEach(IntObjConsumer<T> action);

	void forEach(Consumer<T> action);

	void fill(IntFunction<T> mapper);

	void fill(Supplier<T> mapper);

}
