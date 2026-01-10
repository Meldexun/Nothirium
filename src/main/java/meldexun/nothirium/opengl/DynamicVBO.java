package meldexun.nothirium.opengl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.util.SectorizedList;
import meldexun.nothirium.util.SectorizedList.Sector;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.GLUtil;

public class DynamicVBO {

	private final int vertexSize;
	private final int vertexCountPerSector;
	private final int sectorSize;
	private final SectorizedList sectors;
	private int vbo;
	private final List<Runnable> listeners = new ArrayList<>();

	public DynamicVBO(int vertexSize, int vertexCountPerSector, int sectorCount) {
		this.vertexSize = vertexSize;
		this.vertexCountPerSector = vertexCountPerSector;
		this.sectorSize = vertexCountPerSector * vertexSize;
		this.sectors = new SectorizedList(sectorCount) {
			@Override
			protected void grow(int minContinousSector) {
				int oldSectorCount = this.getSectorCount();
				super.grow(minContinousSector);

				int newVbo = GLHelper.growBuffer(vbo, (long) sectorSize * oldSectorCount,
						(long) sectorSize * getSectorCount());
				if (newVbo != vbo) {
					vbo = newVbo;
					listeners.forEach(Runnable::run);
				}
			}
		};
		long size = (long) sectorSize * sectorCount;
		if (GLUtil.CAPS.OpenGL45) {
			this.vbo = GL45.glCreateBuffers();
			GL45.glNamedBufferStorage(this.vbo, size, GL44.GL_DYNAMIC_STORAGE_BIT);
		} else if (GLUtil.CAPS.GL_ARB_direct_state_access) {
			this.vbo = ARBDirectStateAccess.glCreateBuffers();
			ARBDirectStateAccess.glNamedBufferStorage(this.vbo, size, GL44.GL_DYNAMIC_STORAGE_BIT);
		} else if (GLUtil.CAPS.OpenGL44) {
			this.vbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, size, GL44.GL_DYNAMIC_STORAGE_BIT);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		} else if (GLUtil.CAPS.GL_ARB_buffer_storage) {
			this.vbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			ARBBufferStorage.glBufferStorage(GL15.GL_ARRAY_BUFFER, size, ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		} else {
			this.vbo = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, GL15.GL_DYNAMIC_COPY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
	}

	public void addListener(Runnable listener) {
		this.listeners.add(listener);
	}

	public void removeListener(Runnable listener) {
		this.listeners.remove(listener);
	}

	public VBOPart buffer(ByteBuffer data) {
		int size = data.limit();
		int requiredSectors = MathUtil.ceilDiv(size, this.sectorSize);
		if (requiredSectors <= 0) {
			throw new IllegalArgumentException();
		}
		Sector sector = this.sectors.claim(requiredSectors);
		long offset = (long) sectorSize * sector.getFirstSector();
		if (GLUtil.CAPS.OpenGL45) {
			GL45.glNamedBufferSubData(this.vbo, offset, data);
		} else if (GLUtil.CAPS.GL_ARB_direct_state_access) {
			ARBDirectStateAccess.glNamedBufferSubData(this.vbo, offset, data);
		} else {
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, data);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
		return new VBOPart(sector, size / this.vertexSize);
	}

	public void free(Sector sector) {
		this.sectors.free(sector);
	}

	public int getVbo() {
		return this.vbo;
	}

	public void dispose() {
		GL15.glDeleteBuffers(this.vbo);
	}

	public class VBOPart implements IVBOPart {

		private final Sector sector;
		private final int vertexFirst;
		private final int vertexCount;
		private boolean valid = true;

		private VBOPart(Sector sector, int vertexCount) {
			this.sector = sector;
			this.vertexFirst = sector.getFirstSector() * vertexCountPerSector;
			this.vertexCount = vertexCount;
		}

		@Override
		public int getVBO() {
			return DynamicVBO.this.vbo;
		}

		@Override
		public int getFirst() {
			return this.vertexFirst;
		}

		@Override
		public int getCount() {
			return this.vertexCount;
		}

		@Override
		public int getOffset() {
			return this.vertexFirst * DynamicVBO.this.vertexSize;
		}

		@Override
		public int getSize() {
			return this.vertexCount * DynamicVBO.this.vertexSize;
		}

		@Override
		public void free() {
			if (this.valid) {
				DynamicVBO.this.free(this.sector);
				this.valid = false;
			}
		}

		@Override
		public boolean isValid() {
			return this.valid;
		}

	}

}
