package meldexun.nothirium.mc.renderer.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.renderlib.util.Frustum;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;

public class ChunkRendererGL20 extends ChunkRendererDynamicVbo {

	private final Enum2ObjMap<ChunkRenderPass, List<RenderChunk>> chunks = new Enum2ObjMap<>(ChunkRenderPass.class, (Supplier<List<RenderChunk>>) ArrayList::new);

	@Override
	public void init(int renderDistance) {
		// nothing to do
	}

	@Override
	public void setup(IRenderChunkProvider<RenderChunk> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		resetChunkLists();

		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, frame);
	}

	protected void resetChunkLists() {
		chunks.forEach(List::clear);
	}

	@Override
	protected void record(RenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
		if (renderChunk.isEmpty())
			return;

		for (ChunkRenderPass pass : ChunkRenderPass.ALL) {
			if (renderChunk.getVBOPart(pass) == null)
				continue;

			getChunkListFor(pass).add(renderChunk);
		}
	}

	protected List<RenderChunk> getChunkListFor(ChunkRenderPass pass) {
		return chunks.get(pass);
	}

	@Override
	public void render(ChunkRenderPass pass) {
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos.get(pass).getVbo());

		setupClientState(pass);

		setupAttributePointers(pass);

		List<RenderChunk> list = getChunkListFor(pass);
		if (pass != ChunkRenderPass.TRANSLUCENT) {
			for (int i = 0; i < list.size(); i++) {
				RenderChunk renderChunk = list.get(i);
				IVBOPart vboPart = renderChunk.getVBOPart(pass);
				GL11.glPushMatrix();
				GL11.glTranslated(renderChunk.getX() - RenderUtil.getCameraEntityX(), renderChunk.getY() - RenderUtil.getCameraEntityY(), renderChunk.getZ() - RenderUtil.getCameraEntityZ());
				GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getFirst(), vboPart.getCount());
				GL11.glPopMatrix();
			}
		} else {
			for (int i = list.size() - 1; i >= 0; i--) {
				RenderChunk renderChunk = list.get(i);
				IVBOPart vboPart = renderChunk.getVBOPart(pass);
				GL11.glPushMatrix();
				GL11.glTranslated(renderChunk.getX() - RenderUtil.getCameraEntityX(), renderChunk.getY() - RenderUtil.getCameraEntityY(), renderChunk.getZ() - RenderUtil.getCameraEntityZ());
				GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getFirst(), vboPart.getCount());
				GL11.glPopMatrix();
			}
		}

		resetClientState(pass);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		Minecraft.getMinecraft().entityRenderer.disableLightmap();
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

	protected void resetClientState(ChunkRenderPass pass) {
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
	}

}
