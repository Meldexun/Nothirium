package meldexun.nothirium.util.collection;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IEnumMap<E extends Enum<E>, T> {

	T get(E e);

	void set(E e, T t);

	Stream<T> stream();

	void forEach(BiConsumer<E, T> action);

	void forEach(Consumer<T> action);

	void fill(Function<E, T> mapper);

	void fill(Supplier<T> mapper);

}
