package meldexun.nothirium.mc.renderer.chunk;

import org.lwjgl.opengl.GL15;

import meldexun.memoryutil.UnsafeByteBuffer;
import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import meldexun.nothirium.util.VertexSortUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class RenderChunkTaskSortTranslucent extends AbstractRenderChunkTask<RenderChunk> {

	private final IVBOPart vboPart;
	private final UnsafeByteBuffer vertexData;

	public RenderChunkTaskSortTranslucent(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher,
			RenderChunk renderChunk, IVBOPart vboPart, UnsafeByteBuffer vertexData) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.vboPart = vboPart;
		this.vertexData = vertexData;
	}

	@Override
	public RenderChunkTaskResult run() {
		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}
		if (!vboPart.isValid()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (entity == null) {
			return RenderChunkTaskResult.CANCELLED;
		}

		Vec3d camera = entity.getPositionEyes(1.0F);
		VertexSortUtil.sortVertexData(vertexData, vboPart.getCount(), DefaultVertexFormats.BLOCK.getSize(), 4,
				(float) (renderChunk.getX() - camera.x), (float) (renderChunk.getY() - camera.y), (float) (renderChunk.getZ() - camera.z));

		taskDispatcher.runOnRenderThread(() -> {
			if (!this.canceled() && vboPart.isValid()) {
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
				GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getFirst() * DefaultVertexFormats.BLOCK.getSize(),
						vertexData.getBuffer());
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
		});

		return RenderChunkTaskResult.SUCCESSFUL;
	}

}
