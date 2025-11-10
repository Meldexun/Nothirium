package meldexun.nothirium.util.cache;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IntArrayCache {

	private final Queue<int[]> cache = new ConcurrentLinkedQueue<>();
	private final int arraySize;
	private final int filler;

	public IntArrayCache(int arraySize, int filler) {
		this.arraySize = arraySize;
		this.filler = filler;
	}

	public int[] get() {
		int[] a = cache.poll();
		if (a == null) {
			Arrays.fill((a = new int[arraySize]), filler);
		}
		return a;
	}

	public void free(int[] a) {
		Arrays.fill(a, filler);
		cache.add(a);
	}

}
