package meldexun.nothirium.mc.vertex;

public interface VertexConsumer {

	default void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
		throw new UnsupportedOperationException();
	}

	default void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException();
	}

	default void tex(ExtendedBufferBuilder buffer, double u, double v) {
		throw new UnsupportedOperationException();
	}

	default void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
		throw new UnsupportedOperationException();
	}

	default void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
		throw new UnsupportedOperationException();
	}

}
