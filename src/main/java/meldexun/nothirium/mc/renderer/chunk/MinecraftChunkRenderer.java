package meldexun.nothirium.mc.renderer.chunk;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.renderer.chunk.AbstractChunkRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public abstract class MinecraftChunkRenderer extends AbstractChunkRenderer<RenderChunk> {

	@Override
	public final void render(ChunkRenderPass pass) {
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();

		this.renderChunks(pass);

		GlStateManager.resetColor();
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
	}

	protected abstract void renderChunks(ChunkRenderPass pass);

	@Override
	protected boolean isSpectator() {
		Minecraft mc = Minecraft.getMinecraft();
		Entity cameraEntity = mc.getRenderViewEntity();
		return cameraEntity instanceof EntityPlayer && ((EntityPlayer) cameraEntity).isSpectator();
	}

}
