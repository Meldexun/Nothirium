package meldexun.nothirium.mc.renderer.chunk;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.mc.config.NothiriumConfig.RenderEngine;
import meldexun.nothirium.util.ListUtil;
import meldexun.renderlib.util.RenderUtil;

public class ChunkRendererGL15 extends ChunkRendererDynamicVbo {

	@Override
	public RenderEngine getRenderEngine() {
		return RenderEngine.GL15;
	}

	@Override
	public String name() {
		return "Nothirium GL 1.5";
	}

	@Override
	public void init(int renderDistance) {
		// nothing to do
	}

	@Override
	protected void renderChunks(ChunkRenderPass pass) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos.get(pass).getVbo());
		setupClientState(pass);
		setupAttributePointers(pass);

		double cameraX = RenderUtil.getCameraEntityX();
		double cameraY = RenderUtil.getCameraEntityY();
		double cameraZ = RenderUtil.getCameraEntityZ();
		ListUtil.forEach(chunks.get(pass), pass == ChunkRenderPass.TRANSLUCENT, (renderChunk, i) -> {
			this.draw(renderChunk, pass, cameraX, cameraY, cameraZ);
		});

		resetClientState(pass);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	protected void setupClientState(ChunkRenderPass pass) {
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
	}

	protected void setupAttributePointers(ChunkRenderPass pass) {
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
		GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
	}

	protected void draw(RenderChunk renderChunk, ChunkRenderPass pass, double cameraX, double cameraY, double cameraZ) {
		IVBOPart vboPart = renderChunk.getVBOPart(pass);
		GL11.glPushMatrix();
		GL11.glTranslated(renderChunk.getX() - cameraX, renderChunk.getY() - cameraY, renderChunk.getZ() - cameraZ);
		GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getFirst(), vboPart.getCount());
		GL11.glPopMatrix();
	}

	protected void resetClientState(ChunkRenderPass pass) {
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
	}

}
