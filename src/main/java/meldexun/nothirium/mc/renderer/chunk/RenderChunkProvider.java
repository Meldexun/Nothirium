package meldexun.nothirium.mc.renderer.chunk;

import meldexun.nothirium.renderer.chunk.AbstractRenderChunkProvider;

public class RenderChunkProvider extends AbstractRenderChunkProvider<RenderChunk> {

	@Override
	protected RenderChunk createRenderChunk(int x, int y, int z) {
		return new RenderChunk(x, y, z);
	}

}
