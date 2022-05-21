package meldexun.nothirium.api.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.util.Direction;

public interface IRenderChunk<N extends IRenderChunk<N>> {

	int getX();

	int getY();

	int getZ();

	void setCoords(int x, int y, int z);

	@Nullable
	N getNeighbor(Direction direction);

	void setNeighbor(Direction direction, @Nullable N neighbor);

	@Nullable
	IVBOPart getVBOPart(ChunkRenderPass pass);

	void setVBOPart(ChunkRenderPass pass, @Nullable IVBOPart vboPart);

	boolean isEmpty();

}
