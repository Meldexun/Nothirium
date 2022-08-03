package meldexun.nothirium.mc.renderer.chunk;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Vector3f;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.ChunkAnimator;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class ChunkRendererGL20 extends ChunkRendererDynamicVbo {

	@Override
	public String name() {
		return "Nothirium GL 2.0";
	}

	@Override
	public void init(int renderDistance) {
		// nothing to do
	}

	@Override
	public void render(ChunkRenderPass pass) {
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos.get(pass).getVbo());
		setupClientState(pass);
		setupAttributePointers(pass);

		List<RenderChunk> list = chunks.get(pass);
		double cameraX = RenderUtil.getCameraEntityX();
		double cameraY = RenderUtil.getCameraEntityY();
		double cameraZ = RenderUtil.getCameraEntityZ();
		if (pass != ChunkRenderPass.TRANSLUCENT) {
			for (int i = 0; i < list.size(); i++) {
				this.draw(list.get(i), pass, cameraX, cameraY, cameraZ);
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				this.draw(list.get(list.size() - 1 - i), pass, cameraX, cameraY, cameraZ);
			}
		}

		resetClientState(pass);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GlStateManager.resetColor();
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

	protected void draw(RenderChunk renderChunk, ChunkRenderPass pass, double cameraX, double cameraY, double cameraZ) {
		if (Nothirium.isChunkAnimatorInstalled) {
			Vector3f offset = ChunkAnimator.getOffset(renderChunk);
			cameraX -= offset.x;
			cameraY -= offset.y;
			cameraZ -= offset.z;
		}
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

	@Override
	protected boolean isSpectator() {
		Minecraft mc = Minecraft.getMinecraft();
		Entity cameraEntity = mc.getRenderViewEntity();
		return cameraEntity instanceof EntityPlayer && ((EntityPlayer) cameraEntity).isSpectator();
	}

}
