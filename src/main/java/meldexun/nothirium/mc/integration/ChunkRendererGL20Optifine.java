package meldexun.nothirium.mc.integration;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL20;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.renderlib.util.Frustum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ChunkRendererGL20Optifine extends ChunkRendererGL20 {

	@Override
	public String name() {
		return "Nothirium GL 2.0 (Optifine)";
	}

	@Override
	public void setup(IRenderChunkProvider<RenderChunk> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		if (Optifine.IS_DYNAMIC_LIGHTS.invoke(null)) {
			Optifine.DYNAMIC_LIGHTS_UPDATE.invoke(null, Minecraft.getMinecraft().renderGlobal);
		}

		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, Optifine.IS_SHADOW_PASS.getBoolean(null) ? -frame : frame);
	}

	@Override
	protected void renderChunks(ChunkRenderPass pass) {
		if (Optifine.IS_FOG_OFF.invoke(null) && Optifine.FOG_STANDARD.getBoolean(Minecraft.getMinecraft().entityRenderer)) {
			GlStateManager.disableFog();
		}

		super.renderChunks(pass);
	}

}
