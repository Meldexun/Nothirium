package meldexun.nothirium.opengl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.util.SectorizedList;
import meldexun.nothirium.util.SectorizedList.Sector;
import meldexun.nothirium.util.math.MathUtil;

public class DynamicVBO {

	private final int vertexSize;
	private final int vertexCountPerSector;
	private final int sectorSize;
	private final SectorizedList sectors;
	private int vbo;

	public DynamicVBO(int vertexSize, int vertexCountPerSector, int sectorCount) {
		this.vertexSize = vertexSize;
		this.vertexCountPerSector = vertexCountPerSector;
		this.sectorSize = vertexCountPerSector * vertexSize;
		this.sectors = new SectorizedList(sectorCount) {
			@Override
			protected void grow(int minContinousSector) {
				int oldSectorCount = this.getSectorCount();
				super.grow(minContinousSector);

				GLHelper.growBuffer(vbo, (long) sectorSize * oldSectorCount, (long) sectorSize * getSectorCount());
			}
		};
		this.vbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) sectorSize * sectorCount, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public VBOPart buffer(ByteBuffer data) {
		int size = data.limit();
		int requiredSectors = MathUtil.ceilDiv(size, this.sectorSize);
		if (requiredSectors <= 0) {
			throw new IllegalArgumentException();
		}
		Sector sector = this.sectors.claim(requiredSectors);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) sectorSize * sector.getFirstSector(), data);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
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
