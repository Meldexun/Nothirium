package meldexun.nothirium.api.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.SectionPos;

public interface IRenderChunk<N extends IRenderChunk<N>> {

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
	N getNeighbor(Direction direction);

	void setNeighbor(Direction direction, @Nullable N neighbor);

	@Nullable
	IVBOPart getVBOPart(ChunkRenderPass pass);

	void setVBOPart(ChunkRenderPass pass, @Nullable IVBOPart vboPart);

	boolean isEmpty();

}
