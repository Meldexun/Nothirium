package meldexun.nothirium.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import meldexun.nothirium.mc.Nothirium;
import meldexun.renderlib.util.GLUtil;
import net.minecraft.client.renderer.GlStateManager;

public class GLTest {

	public static boolean glDrawArraysInstanced;
	public static boolean glDrawArraysIndirect;
	public static boolean glMultiDrawArraysIndirect;

	public static void runTests() {
		GlStateManager.disableAlpha();
		GlStateManager.disableDepth();

		ByteBuffer data = buffer(-0.5F, -0.5F, 0.0F, -0.5F, 0.0F, 0.5F, -0.5F, 0.5F);
		GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, data);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		int expected = samplesPassed(() -> GL11.glDrawArrays(GL11.GL_QUADS, 0, 4));

		glDrawArraysInstanced = GLUtil.CAPS.OpenGL31 && samplesPassed(() -> {
			GL31.glDrawArraysInstanced(GL11.GL_QUADS, 0, 4, 1);
		}) == expected;
		Nothirium.LOGGER.info("Test glDrawArraysInstanced: {}", glDrawArraysInstanced);

		glDrawArraysIndirect = GLUtil.CAPS.OpenGL40 && samplesPassed(() -> {
			GL40.glDrawArraysIndirect(GL11.GL_QUADS, buffer(4, 1, 0, 0));
		}) == expected;
		Nothirium.LOGGER.info("Test glDrawArraysIndirect: {}", glDrawArraysIndirect);

		glMultiDrawArraysIndirect = GLUtil.CAPS.OpenGL43 && samplesPassed(() -> {
			GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, buffer(4, 1, 0, 0), 1, 0);
		}) == expected;
		Nothirium.LOGGER.info("Test glMultiDrawArraysIndirect: {}", glMultiDrawArraysIndirect);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	private static ByteBuffer buffer(float... floats) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(floats.length << 2).order(ByteOrder.nativeOrder());
		for (float f : floats)
			buffer.putFloat(f);
		buffer.flip();
		return buffer;
	}

	private static ByteBuffer buffer(int... ints) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(ints.length << 2).order(ByteOrder.nativeOrder());
		for (int i : ints)
			buffer.putInt(i);
		buffer.flip();
		return buffer;
	}

	private static int samplesPassed(Runnable drawCall) {
		int query = GL15.glGenQueries();
		try {
			GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, query);
			drawCall.run();
			GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
			return GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT);
		} finally {
			GL15.glDeleteQueries(query);
		}
	}

}
