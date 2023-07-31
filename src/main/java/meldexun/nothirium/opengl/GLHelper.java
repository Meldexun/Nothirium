package meldexun.nothirium.opengl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL45;

import meldexun.renderlib.util.GLUtil;

public class GLHelper {

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
			ByteBuffer tempBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, oldSize, null);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
			GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, tempBuffer);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newSize, GL15.GL_STREAM_DRAW);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, tempBuffer);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
			GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glDeleteBuffers(temp);
			return vbo;
		}
	}

}
