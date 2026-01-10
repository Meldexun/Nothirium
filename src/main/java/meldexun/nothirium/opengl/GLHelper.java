package meldexun.nothirium.opengl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBCopyBuffer;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import meldexun.memoryutil.NIOBufferUtil;
import meldexun.renderlib.util.GLUtil;

public class GLHelper {

	public static int growBuffer(int vbo, long oldSize, long newSize) {
		if (GLUtil.CAPS.OpenGL45) {
			int newVbo = GL45.glCreateBuffers();
			GL45.glNamedBufferStorage(newVbo, newSize, GL44.GL_DYNAMIC_STORAGE_BIT);
			GL45.glCopyNamedBufferSubData(vbo, newVbo, 0L, 0L, oldSize);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.GL_ARB_direct_state_access) {
			int newVbo = ARBDirectStateAccess.glCreateBuffers();
			ARBDirectStateAccess.glNamedBufferStorage(newVbo, newSize, GL44.GL_DYNAMIC_STORAGE_BIT);
			ARBDirectStateAccess.glCopyNamedBufferSubData(vbo, newVbo, 0L, 0L, oldSize);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.OpenGL44) {
			int newVbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
			GL44.glBufferStorage(GL31.GL_COPY_WRITE_BUFFER, newSize, GL44.GL_DYNAMIC_STORAGE_BIT);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.GL_ARB_buffer_storage) {
			int newVbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
			ARBBufferStorage.glBufferStorage(GL31.GL_COPY_WRITE_BUFFER, newSize, ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.OpenGL31) {
			int newVbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
			GL15.glBufferData(GL31.GL_COPY_WRITE_BUFFER, newSize, GL15.GL_DYNAMIC_COPY);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
			GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else if (GLUtil.CAPS.GL_ARB_copy_buffer) {
			int newVbo = GL15.glGenBuffers();
			GL15.glBindBuffer(ARBCopyBuffer.GL_COPY_WRITE_BUFFER, newVbo);
			GL15.glBufferData(ARBCopyBuffer.GL_COPY_WRITE_BUFFER, newSize, GL15.GL_DYNAMIC_COPY);
			GL15.glBindBuffer(ARBCopyBuffer.GL_COPY_READ_BUFFER, vbo);
			ARBCopyBuffer.glCopyBufferSubData(ARBCopyBuffer.GL_COPY_READ_BUFFER, ARBCopyBuffer.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
			GL15.glBindBuffer(ARBCopyBuffer.GL_COPY_READ_BUFFER, 0);
			GL15.glBindBuffer(ARBCopyBuffer.GL_COPY_WRITE_BUFFER, 0);
			GL15.glDeleteBuffers(vbo);
			return newVbo;
		} else {
			ByteBuffer temp = NIOBufferUtil.allocateByte(oldSize);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
			GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, temp);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newSize, GL15.GL_DYNAMIC_COPY);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, temp);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			NIOBufferUtil.freeMemory(temp);
			return vbo;
		}
	}

}
