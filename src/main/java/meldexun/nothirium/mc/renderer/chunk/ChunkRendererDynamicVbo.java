package meldexun.nothirium.mc.renderer.chunk;

import java.nio.ByteBuffer;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.opengl.DynamicVBO;
import meldexun.nothirium.renderer.chunk.AbstractChunkRenderer;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public abstract class ChunkRendererDynamicVbo extends AbstractChunkRenderer<RenderChunk> {

	protected final Enum2ObjMap<ChunkRenderPass, DynamicVBO> vbos = new Enum2ObjMap<>(ChunkRenderPass.class,
			i -> new DynamicVBO(DefaultVertexFormats.BLOCK.getSize(), 128, 4096));

	@Override
	public void dispose() {
		vbos.forEach(DynamicVBO::dispose);
	}

	@Override
	public IVBOPart buffer(ChunkRenderPass pass, ByteBuffer data) {
		return vbos.get(pass).buffer(data);
	}

}
