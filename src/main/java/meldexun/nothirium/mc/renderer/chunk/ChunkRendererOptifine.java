package meldexun.nothirium.mc.renderer.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.util.BlockRenderLayerUtil;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import meldexun.renderlib.util.Frustum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockRenderLayer;

public class ChunkRendererOptifine extends ChunkRendererGL20 {

	private static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	private static final ReflectionMethod<Boolean> IS_FOG_OFF = new ReflectionMethod<>("Config", "isFogOff", "isFogOff");
	private static final ReflectionField<Boolean> FOG_STANDARD = new ReflectionField<>("net.minecraft.client.renderer.EntityRenderer", "fogStandard", "fogStandard");
	private static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	private static final ReflectionMethod<Void> PRE_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "preRenderChunkLayer", "preRenderChunkLayer", BlockRenderLayer.class);
	private static final ReflectionMethod<Void> SETUP_ARRAY_POINTERS_VBO = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "setupArrayPointersVbo", "setupArrayPointersVbo");
	private static final ReflectionMethod<Void> POST_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "postRenderChunkLayer", "postRenderChunkLayer", BlockRenderLayer.class);
	private final Enum2ObjMap<ChunkRenderPass, List<RenderChunk>> shadow_chunks = new Enum2ObjMap<>(ChunkRenderPass.class, (Supplier<List<RenderChunk>>) ArrayList::new);

	@Override
	public void setup(IRenderChunkProvider<RenderChunk> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, IS_SHADOW_PASS.getBoolean(null) ? -frame : frame);
	}

	@Override
	protected void resetChunkLists() {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			shadow_chunks.forEach(List::clear);
		} else {
			super.resetChunkLists();
		}
	}

	@Override
	protected List<RenderChunk> getChunkListFor(ChunkRenderPass pass) {
		if (IS_SHADOW_PASS.getBoolean(null)) {
			return shadow_chunks.get(pass);
		}

		return super.getChunkListFor(pass);
	}

	@Override
	public void render(ChunkRenderPass pass) {
		if (IS_FOG_OFF.invoke(null) && FOG_STANDARD.getBoolean(Minecraft.getMinecraft().entityRenderer)) {
			GlStateManager.disableFog();
		}

		super.render(pass);
	}

	@Override
	protected void setupClientState(ChunkRenderPass pass) {
		super.setupClientState(pass);

		if (IS_SHADERS.invoke(null)) {
			PRE_RENDER_CHUNK_LAYER.invoke(null, BlockRenderLayerUtil.getBlockRenderLayer(pass));
		}
	}

	@Override
	protected void setupAttributePointers(ChunkRenderPass pass) {
		if (IS_SHADERS.invoke(null)) {
			SETUP_ARRAY_POINTERS_VBO.invoke(null);
		} else {
			super.setupAttributePointers(pass);
		}
	}

	@Override
	protected void resetClientState(ChunkRenderPass pass) {
		if (IS_SHADERS.invoke(null)) {
			POST_RENDER_CHUNK_LAYER.invoke(null, BlockRenderLayerUtil.getBlockRenderLayer(pass));
		}

		super.resetClientState(pass);
	}

}
