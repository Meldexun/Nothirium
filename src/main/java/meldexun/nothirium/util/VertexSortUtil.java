package meldexun.nothirium.util;

import java.util.BitSet;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntComparators;
import meldexun.memoryutil.MemoryAccess;
import meldexun.memoryutil.UnsafeBufferUtil;

public class VertexSortUtil {

	public static void sortVertexData(MemoryAccess vertexData, int vertexCount, int vertexSize, int primitiveSize, float dx, float dy, float dz) {
		if (vertexCount == 0) {
			return;
		}

		int[] primitiveOrder = calculatePrimitiveOrder(vertexData, vertexCount, vertexSize, primitiveSize, dx, dy, dz);

		int bytesPerPrimitive = vertexSize * primitiveSize;
		UnsafeBufferUtil.tempBuffer(bytesPerPrimitive, tempBuffer -> {
			BitSet sortedPrimitivess = new BitSet();
			for (int i1 = sortedPrimitivess.nextClearBit(0); i1 < primitiveOrder.length; i1 = sortedPrimitivess.nextClearBit(i1 + 1)) {
				int j1 = primitiveOrder[i1];

				if (j1 != i1) {
					MemoryAccess.copyMemory(vertexData, j1 * bytesPerPrimitive, tempBuffer, 0L, bytesPerPrimitive);
					int k1 = j1;

					for (int l1 = primitiveOrder[j1]; k1 != i1; l1 = primitiveOrder[l1]) {
						MemoryAccess.copyMemory(vertexData, l1 * bytesPerPrimitive, vertexData, k1 * bytesPerPrimitive, bytesPerPrimitive);
						sortedPrimitivess.set(k1);
						k1 = l1;
					}

					MemoryAccess.copyMemory(tempBuffer, 0L, vertexData, i1 * bytesPerPrimitive, bytesPerPrimitive);
				}

				sortedPrimitivess.set(i1);
			}
		});
	}

	private static int[] calculatePrimitiveOrder(MemoryAccess vertexData, int vertexCount, int vertexSize, int primitiveSize, float dx, float dy, float dz) {
		float[] primitiveSqrDistTo = calculatePrimitiveSqrDistTo(vertexData, vertexCount, vertexSize, primitiveSize, dx, dy, dz);

		int[] primitiveOrder = IntStream.range(0, vertexCount / primitiveSize).toArray();
		IntArrays.mergeSort(primitiveOrder, IntComparators.oppositeComparator(comparingFloat(i -> primitiveSqrDistTo[i])));

		return primitiveOrder;
	}

	private static float[] calculatePrimitiveSqrDistTo(MemoryAccess vertexData, int vertexCount, int vertexSize, int primitiveSize, float dx, float dy, float dz) {
		float[] primitiveSqrDistTo = new float[vertexCount / primitiveSize];

		for (int i = 0; i < vertexCount / primitiveSize; i++) {
			float x = 0.0F;
			float y = 0.0F;
			float z = 0.0F;
			for (int j = 0; j < primitiveSize; j++) {
				x += vertexData.getFloat((i * primitiveSize + j) * vertexSize);
				y += vertexData.getFloat((i * primitiveSize + j) * vertexSize + 4);
				z += vertexData.getFloat((i * primitiveSize + j) * vertexSize + 8);
			}
			x /= primitiveSize;
			y /= primitiveSize;
			z /= primitiveSize;
			x += dx;
			y += dy;
			z += dz;
			primitiveSqrDistTo[i] = x * x + y * y + z * z;
		}

		return primitiveSqrDistTo;
	}

	private static IntComp comparingFloat(Int2FloatFunc f) {
		return (x, y) -> Float.compare(f.apply(x), f.apply(y));
	}

	@FunctionalInterface
	private interface IntComp extends IntComparator {

		@Override
		default int compare(Integer o1, Integer o2) {
			return compare((int) o1, (int) o2);
		}

	}

	@FunctionalInterface
	private interface Int2FloatFunc {

		float apply(int x);

	}

}
