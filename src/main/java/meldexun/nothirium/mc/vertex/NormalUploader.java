package meldexun.nothirium.mc.vertex;

import meldexun.memoryutil.UnsafeUtil;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import sun.misc.Unsafe;

public class NormalUploader {

	private static final VertexConsumer FLOAT = new VertexConsumer() {
		@Override
		public void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putFloat(address, x);
			unsafe.putFloat(address + 4, y);
			unsafe.putFloat(address + 8, z);
		}
	};
	private static final VertexConsumer INT = new VertexConsumer() {
		@Override
		public void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putInt(address, (int) x);
			unsafe.putInt(address + 4, (int) y);
			unsafe.putInt(address + 8, (int) z);
		}
	};
	private static final VertexConsumer SHORT = new VertexConsumer() {
		@Override
		public void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putShort(address, (short) ((int) (x * 32767) & 65535));
			unsafe.putShort(address + 2, (short) ((int) (y * 32767) & 65535));
			unsafe.putShort(address + 4, (short) ((int) (z * 32767) & 65535));
		}
	};
	private static final VertexConsumer BYTE = new VertexConsumer() {
		@Override
		public void normal(ExtendedBufferBuilder buffer, float x, float y, float z) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putByte(address, (byte) ((int) (x * 127) & 255));
			unsafe.putByte(address + 1, (byte) ((int) (y * 127) & 255));
			unsafe.putByte(address + 2, (byte) ((int) (z * 127) & 255));
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
