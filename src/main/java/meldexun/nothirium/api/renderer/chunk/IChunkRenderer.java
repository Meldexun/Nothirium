package meldexun.nothirium.api.renderer.chunk;

import java.nio.ByteBuffer;
import java.util.List;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.renderlib.util.Frustum;

public interface IChunkRenderer<T extends IRenderChunk> {

	String name();

	int renderedChunks();

	int renderedChunks(ChunkRenderPass pass);

	List<T> getRenderChunks();

	void init(int renderDistance);

	void setup(IRenderChunkProvider<T> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame);

	void render(ChunkRenderPass pass);

	IVBOPart buffer(ChunkRenderPass pass, ByteBuffer data);

	void dispose();

}
