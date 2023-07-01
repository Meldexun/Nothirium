package meldexun.nothirium.mc.integration;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL42;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.renderlib.util.Frustum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ChunkRendererGL42Optifine extends ChunkRendererGL42 {

	@Override
	public String name() {
		return "Nothirium GL 4.2 (Optifine)";
	}

	@Override
	public void setup(IRenderChunkProvider<RenderChunk> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		if (Optifine.IS_DYNAMIC_LIGHTS.invoke(null)) {
			Optifine.DYNAMIC_LIGHTS_UPDATE.invoke(null, Minecraft.getMinecraft().renderGlobal);
		}

		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, frame);
	}

	@Override
	protected void renderChunks(ChunkRenderPass pass) {
		if (Optifine.IS_FOG_OFF.invoke(null) && Optifine.FOG_STANDARD.getBoolean(Minecraft.getMinecraft().entityRenderer)) {
			GlStateManager.disableFog();
		}

		super.renderChunks(pass);
	}

}
