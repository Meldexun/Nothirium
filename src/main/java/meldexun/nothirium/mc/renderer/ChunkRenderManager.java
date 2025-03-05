package meldexun.nothirium.mc.renderer;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.config.NothiriumConfig;
import meldexun.nothirium.mc.config.NothiriumConfig.RenderEngine;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL15;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL20;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL42;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL43;
import meldexun.nothirium.mc.renderer.chunk.MinecraftChunkRenderer;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkDispatcher;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkProvider;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;

public class ChunkRenderManager {

	private static IChunkRenderer<?> chunkRenderer;
	private static IRenderChunkProvider<?> renderChunkProvider;
	private static IRenderChunkDispatcher taskDispatcher;

	@SuppressWarnings("unchecked")
	public static <T extends IChunkRenderer<?>> T getRenderer() {
		return (T) chunkRenderer;
	}

	@SuppressWarnings("unchecked")
	public static <T extends IRenderChunkProvider<?>> T getProvider() {
		return (T) renderChunkProvider;
	}

	@SuppressWarnings("unchecked")
	public static <T extends IRenderChunkDispatcher> T getTaskDispatcher() {
		return (T) taskDispatcher;
	}

	public static void allChanged() {
		chunkRenderer = createChunkRenderer(chunkRenderer);
		if (renderChunkProvider != null) {
			renderChunkProvider.releaseBuffers();
		} else {
			renderChunkProvider = new RenderChunkProvider();
		}
		if (taskDispatcher == null) {
			taskDispatcher = new RenderChunkDispatcher();
		}

		Minecraft mc = Minecraft.getMinecraft();
		int renderDistance = mc.gameSettings.renderDistanceChunks;
		renderChunkProvider.init(renderDistance, renderDistance, renderDistance);
		chunkRenderer.init(renderDistance);
	}

	private static IChunkRenderer<?> createChunkRenderer(@Nullable IChunkRenderer<?> oldChunkRenderer) {
		RenderEngine renderEngine = NothiriumConfig.getRenderEngine();
		if (oldChunkRenderer != null && ((MinecraftChunkRenderer) oldChunkRenderer).getRenderEngine() != renderEngine) {
			oldChunkRenderer.dispose();
			oldChunkRenderer = null;
		}
		if (oldChunkRenderer != null) {
			return oldChunkRenderer;
		}

		switch (renderEngine) {
		case GL43:
			return new ChunkRendererGL43();
		case GL42:
			return new ChunkRendererGL42();
		case GL20:
			return new ChunkRendererGL20();
		case GL15:
			return new ChunkRendererGL15();
		default:
			throw new UnsupportedOperationException();
		}
	}

	public static void dispose() {
		if (chunkRenderer != null) {
			chunkRenderer.dispose();
			chunkRenderer = null;
		}
		if (renderChunkProvider != null) {
			renderChunkProvider.releaseBuffers();
			renderChunkProvider = null;
		}
		if (taskDispatcher != null) {
			taskDispatcher.dispose();
			taskDispatcher = null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setup() {
		taskDispatcher.update();
		renderChunkProvider.repositionCamera(RenderUtil.getCameraX(), RenderUtil.getCameraY(), RenderUtil.getCameraZ());
		chunkRenderer.setup((IRenderChunkProvider) renderChunkProvider, RenderUtil.getCameraX(), RenderUtil.getCameraY(), RenderUtil.getCameraZ(), RenderUtil.getFrustum(), RenderUtil.getFrame());
	}

	public static int renderedSections() {
		return chunkRenderer.renderedChunks();
	}

	public static int renderedSections(ChunkRenderPass pass) {
		return chunkRenderer.renderedChunks(pass);
	}

	public static int totalSections() {
		int r = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 2 + 1;
		return r * r * Math.min(r, 16);
	}

}
