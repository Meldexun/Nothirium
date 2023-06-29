package meldexun.nothirium.api.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.util.SectionPos;

public interface IRenderChunk {

	default int getX() {
		return getPos().getBlockX();
	}

	default int getY() {
		return getPos().getBlockY();
	}

	default int getZ() {
		return getPos().getBlockZ();
	}

	default int getSectionX() {
		return getPos().getX();
	}

	default int getSectionY() {
		return getPos().getY();
	}

	default int getSectionZ() {
		return getPos().getZ();
	}

	SectionPos getPos();

	boolean setCoords(int x, int y, int z);

	@Nullable
	IVBOPart getVBOPart(ChunkRenderPass pass);

	void setVBOPart(ChunkRenderPass pass, @Nullable IVBOPart vboPart);

	boolean isEmpty();

}
