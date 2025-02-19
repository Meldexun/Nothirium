package meldexun.nothirium.mc.vertex;

import java.nio.ByteOrder;

import meldexun.memoryutil.UnsafeUtil;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import sun.misc.Unsafe;

public class ColorUploader {

	private static final VertexConsumer FLOAT = new VertexConsumer() {
		@Override
		public void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putFloat(address, (float) red / 255.0F);
			unsafe.putFloat(address + 4, (float) green / 255.0F);
			unsafe.putFloat(address + 8, (float) blue / 255.0F);
			unsafe.putFloat(address + 12, (float) alpha / 255.0F);
		}
	};
	private static final VertexConsumer INT = new VertexConsumer() {
		@Override
		public void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putFloat(address, (float) red);
			unsafe.putFloat(address + 4, (float) green);
			unsafe.putFloat(address + 8, (float) blue);
			unsafe.putFloat(address + 12, (float) alpha);
		}
	};
	private static final VertexConsumer SHORT = new VertexConsumer() {
		@Override
		public void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
			Unsafe unsafe = UnsafeUtil.UNSAFE;
			long address = buffer.getAddress() + buffer.getOffset();
			unsafe.putShort(address, (short) red);
			unsafe.putShort(address + 2, (short) green);
			unsafe.putShort(address + 4, (short) blue);
			unsafe.putShort(address + 6, (short) alpha);
		}
	};
	private static final VertexConsumer BYTE = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
			? new VertexConsumer() {
				@Override
				public void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
					Unsafe unsafe = UnsafeUtil.UNSAFE;
					long address = buffer.getAddress() + buffer.getOffset();
					unsafe.putByte(address, (byte) red);
					unsafe.putByte(address + 1, (byte) green);
					unsafe.putByte(address + 2, (byte) blue);
					unsafe.putByte(address + 3, (byte) alpha);
				}
			}
			: new VertexConsumer() {
				@Override
				public void color(ExtendedBufferBuilder buffer, int red, int green, int blue, int alpha) {
					Unsafe unsafe = UnsafeUtil.UNSAFE;
					long address = buffer.getAddress() + buffer.getOffset();
					unsafe.putByte(address, (byte) alpha);
					unsafe.putByte(address + 1, (byte) blue);
					unsafe.putByte(address + 2, (byte) green);
					unsafe.putByte(address + 3, (byte) red);
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
