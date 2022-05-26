package meldexun.nothirium.mc.renderer;

import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.asm.NothiriumClassTransformer;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL20;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL43;
import meldexun.nothirium.mc.renderer.chunk.ChunkRendererOptifine;
import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkDispatcher;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkProvider;
import meldexun.nothirium.renderer.chunk.AbstractChunkRenderer;
import meldexun.reflectionutil.ReflectionMethod;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;

public class ChunkRenderManager {

	private static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	private static AbstractChunkRenderer<RenderChunk> chunkRenderer;
	private static RenderChunkProvider renderChunkProvider;
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
		if (chunkRenderer == null) {
			if (NothiriumClassTransformer.OPTIFINE_DETECTED && IS_SHADERS.invoke(null)) {
				chunkRenderer = new ChunkRendererOptifine();
			} else if (Nothirium.isGL43Supported()) {
				chunkRenderer = new ChunkRendererGL43(2);
			} else {
				chunkRenderer = new ChunkRendererGL20();
			}
		} else {
			if (NothiriumClassTransformer.OPTIFINE_DETECTED && Nothirium.isGL43Supported()) {
				if (!IS_SHADERS.invoke(null) && chunkRenderer instanceof ChunkRendererOptifine) {
					chunkRenderer.dispose();
					chunkRenderer = new ChunkRendererGL43(2);
				} else if (IS_SHADERS.invoke(null) && !(chunkRenderer instanceof ChunkRendererOptifine)) {
					chunkRenderer.dispose();
					chunkRenderer = new ChunkRendererOptifine();
				}
			}
		}
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
		renderChunkProvider.init(renderDistance);
		chunkRenderer.init(renderDistance);
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

	public static void setup() {
		taskDispatcher.update();
		renderChunkProvider.repositionCamera(RenderUtil.getCameraX(), RenderUtil.getCameraY(), RenderUtil.getCameraZ());
		chunkRenderer.setup(renderChunkProvider, RenderUtil.getCameraX(), RenderUtil.getCameraY(), RenderUtil.getCameraZ(), RenderUtil.getFrustum(), RenderUtil.getFrame());
	}

}
