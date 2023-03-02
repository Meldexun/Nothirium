package meldexun.nothirium.util.cache;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.IntFunction;

public class ArrayCache<T> {

	private final Queue<T[]> cache = new LinkedList<>();
	private final int arraySize;
	private final IntFunction<T[]> supplier;
	private final T filler;

	public ArrayCache(int arraySize, IntFunction<T[]> supplier, T filler) {
		this.arraySize = arraySize;
		this.supplier = supplier;
		this.filler = filler;
	}

	public T[] get() {
		T[] a;
		synchronized (this) {
			a = cache.poll();
		}
		if (a == null) {
			Arrays.fill((a = supplier.apply(arraySize)), filler);
		}
		return a;
	}

	public void free(T[] t) {
		Arrays.fill(t, filler);
		synchronized (this) {
			cache.add(t);
		}
	}

}
