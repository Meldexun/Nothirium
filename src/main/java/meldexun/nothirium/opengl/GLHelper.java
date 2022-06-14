package meldexun.nothirium.opengl;

import java.nio.Buffer;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL45;

import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import meldexun.renderlib.util.GLUtil;

public class GLHelper {

	private static final ReflectionField<Long> ADDRESS = new ReflectionField<>(Buffer.class, "address", "address");
	private static final ReflectionMethod<Void> NGL_GET_BUFFER_SUB_DATA = new ReflectionMethod<>(GL15.class, "nglGetBufferSubData", "nglGetBufferSubData", int.class, long.class, long.class, long.class, long.class);
	private static final ReflectionMethod<Void> NGL_BUFFER_SUB_DATA = new ReflectionMethod<>(GL15.class, "nglBufferSubData", "nglBufferSubData", int.class, long.class, long.class, long.class, long.class);

	private static long glGetBufferSubData;
	private static long glBufferSubData;

	public static void init() {
		glGetBufferSubData = new ReflectionField<>(ContextCapabilities.class, "glGetBufferSubData", "glGetBufferSubData").getLong(GLUtil.CAPS);
		glBufferSubData = new ReflectionField<>(ContextCapabilities.class, "glBufferSubData", "glBufferSubData").getLong(GLUtil.CAPS);
	}

	public static boolean isGL43Supported() {
		return GLUtil.CAPS.OpenGL43;
	}

	public static void growBuffer(int vbo, long oldSize, long newSize) {
		if (GLUtil.CAPS.OpenGL45) {
			int temp = GL45.glCreateBuffers();
			GL45.glNamedBufferData(temp, oldSize, GL15.GL_STREAM_COPY);
			GL45.glCopyNamedBufferSubData(vbo, temp, 0, 0, oldSize);
			GL45.glNamedBufferData(vbo, newSize, GL15.GL_DYNAMIC_DRAW);
			GL45.glCopyNamedBufferSubData(temp, vbo, 0, 0, oldSize);
			GL15.glDeleteBuffers(temp);
		} else if (GLUtil.CAPS.OpenGL31) {
			int temp = GL15.glGenBuffers();
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, temp);
			GL15.glBufferData(GL31.GL_COPY_READ_BUFFER, oldSize, GL15.GL_STREAM_COPY);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, vbo);
			GL31.glCopyBufferSubData(GL31.GL_COPY_WRITE_BUFFER, GL31.GL_COPY_READ_BUFFER, 0, 0, oldSize);
			GL15.glBufferData(GL31.GL_COPY_WRITE_BUFFER, newSize, GL15.GL_DYNAMIC_DRAW);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0, 0, oldSize);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glDeleteBuffers(temp);
		} else {
			int temp = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, oldSize, GL15.GL_STREAM_COPY);
			long tempAddress = ADDRESS.getLong(GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, oldSize, null));
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
			NGL_GET_BUFFER_SUB_DATA.invoke(null, GL15.GL_ARRAY_BUFFER, 0, oldSize, tempAddress, glGetBufferSubData);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newSize, GL15.GL_DYNAMIC_DRAW);
			NGL_BUFFER_SUB_DATA.invoke(null, GL15.GL_ARRAY_BUFFER, 0, oldSize, tempAddress, glBufferSubData);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glDeleteBuffers(temp);
		}
	}

}
