package meldexun.nothirium.mc.vertex;

import meldexun.memoryutil.UnsafeUtil;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import sun.misc.Unsafe;

public class PositionUploader {

	private static final VertexConsumer FLOAT = new VertexConsumer() {
		@Override
		public void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putFloat(address, (float) (x + buffer.xOffset()));
			unsafe.putFloat(address + 4, (float) (y + buffer.yOffset()));
			unsafe.putFloat(address + 8, (float) (z + buffer.zOffset()));
		}
	};
	private static final VertexConsumer INT = new VertexConsumer() {
		@Override
		public void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putInt(address, Float.floatToRawIntBits((float) (x + buffer.xOffset())));
			unsafe.putInt(address + 4, Float.floatToRawIntBits((float) (y + buffer.yOffset())));
			unsafe.putInt(address + 8, Float.floatToRawIntBits((float) (z + buffer.zOffset())));
		}
	};
	private static final VertexConsumer SHORT = new VertexConsumer() {
		@Override
		public void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putShort(address, (short) ((int) (x + buffer.xOffset())));
			unsafe.putShort(address + 2, (short) ((int) (y + buffer.yOffset())));
			unsafe.putShort(address + 4, (short) ((int) (z + buffer.zOffset())));
		}
	};
	private static final VertexConsumer BYTE = new VertexConsumer() {
		@Override
		public void pos(ExtendedBufferBuilder buffer, double x, double y, double z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putByte(address, (byte) ((int) (x + buffer.xOffset())));
			unsafe.putByte(address + 1, (byte) ((int) (y + buffer.yOffset())));
			unsafe.putByte(address + 2, (byte) ((int) (z + buffer.zOffset())));
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
