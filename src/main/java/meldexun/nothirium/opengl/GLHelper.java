package meldexun.nothirium.opengl;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL45;

import meldexun.matrixutil.MemoryUtil;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import meldexun.renderlib.util.GLUtil;

public class GLHelper {

	private static final ReflectionMethod<Void> NGL_GET_BUFFER_SUB_DATA = new ReflectionMethod<>(GL15.class,
			"nglGetBufferSubData", "nglGetBufferSubData", int.class, long.class, long.class, long.class, long.class);
	private static final ReflectionMethod<Void> NGL_BUFFER_SUB_DATA = new ReflectionMethod<>(GL15.class,
			"nglBufferSubData", "nglBufferSubData", int.class, long.class, long.class, long.class, long.class);

	private static long glGetBufferSubData;
	private static long glBufferSubData;

	public static void init() {
		glGetBufferSubData = new ReflectionField<>(ContextCapabilities.class, "glGetBufferSubData",
				"glGetBufferSubData").getLong(GLUtil.CAPS);
		glBufferSubData = new ReflectionField<>(ContextCapabilities.class, "glBufferSubData", "glBufferSubData")
				.getLong(GLUtil.CAPS);
	}

	private static long glMapBuffer(int target, int access, long length) {
		return MemoryUtil.getAddress(GL15.glMapBuffer(target, access, length, null));
	}

	private static void glGetBufferSubData(int target, long offset, long size, long address) {
		NGL_GET_BUFFER_SUB_DATA.invoke(null, target, offset, size, address, glGetBufferSubData);
	}

	private static void glBufferSubData(int target, long offset, long size, long address) {
		NGL_BUFFER_SUB_DATA.invoke(null, target, offset, size, address, glBufferSubData);
	}

	public static int growBuffer(int vbo, long oldSize, long newSize) {
		if (GLUtil.CAPS.OpenGL45) {
			int newVbo = GL45.glCreateBuffers();
			GL45.glNamedBufferData(newVbo, newSize, GL15.GL_STREAM_DRAW);
			GL45.glCopyNamedBufferSubData(vbo, newVbo, 0L, 0L, oldSize);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.OpenGL31) {
			int newVbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
			GL15.glBufferData(GL31.GL_COPY_WRITE_BUFFER, newSize, GL15.GL_STREAM_DRAW);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else {
			int temp = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, oldSize, GL15.GL_STREAM_COPY);
			long tempAddress = glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, oldSize);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
			glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, oldSize, tempAddress);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newSize, GL15.GL_STREAM_DRAW);
			glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, oldSize, tempAddress);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glDeleteBuffers(temp);
			return vbo;
		}
	}

}
