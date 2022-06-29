package meldexun.nothirium.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferUtil {

	public static ByteBuffer copy(ByteBuffer buffer) {
		int oldPos = buffer.position();
		ByteBuffer copy = ByteBuffer.allocateDirect(buffer.remaining()).order(ByteOrder.nativeOrder());
		copy.put(buffer);
		buffer.position(oldPos);
		copy.position(0);
		return copy;
	}

}
