package meldexun.nothirium.mc.vertex;

import meldexun.matrixutil.UnsafeUtil;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import sun.misc.Unsafe;

public class LightmapCoordinateUploader {

	private static final VertexConsumer FLOAT = new VertexConsumer() {
		@Override
		public void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putFloat(address, (float) skyLight);
			unsafe.putFloat(address + 4, (float) blockLight);
		}
	};
	private static final VertexConsumer INT = new VertexConsumer() {
		@Override
		public void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putInt(address, skyLight);
			unsafe.putInt(address + 4, blockLight);
		}
	};
	private static final VertexConsumer SHORT = new VertexConsumer() {
		@Override
		public void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putShort(address, (short) blockLight);
			unsafe.putShort(address + 2, (short) skyLight);
		}
	};
	private static final VertexConsumer BYTE = new VertexConsumer() {
		@Override
		public void lightmap(ExtendedBufferBuilder buffer, int skyLight, int blockLight) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putByte(address, (byte) blockLight);
			unsafe.putByte(address + 1, (byte) skyLight);
		}
	};

	public static VertexConsumer fromType(VertexFormatElement.EnumType type) {
		switch (type) {
		case FLOAT:
			return FLOAT;
		case UINT:
		case INT:
			return INT;
		case USHORT:
		case SHORT:
			return SHORT;
		case UBYTE:
		case BYTE:
			return BYTE;
		default:
			return null;
		}
	}

}
