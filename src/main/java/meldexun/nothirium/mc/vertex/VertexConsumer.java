package meldexun.nothirium.mc.vertex;

public interface VertexConsumer {

	default void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
		// ignore
	}

	default void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
		// ignore
	}

	default void tex(ExtendedBufferBuilder buffer, double u, double v) {
		// ignore
	}

	default void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
		// ignore
	}

	default void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
		// ignore
	}

}
