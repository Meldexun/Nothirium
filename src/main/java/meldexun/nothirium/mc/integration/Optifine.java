package meldexun.nothirium.mc.integration;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.mc.asm.NothiriumPlugin;
import meldexun.nothirium.opengl.GLTest;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public class Optifine {

	public static final boolean OPTIFINE_DETECTED;
	static {
		boolean flag = false;
		try {
			Class.forName("optifine.OptiFineClassTransformer", false, NothiriumPlugin.class.getClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		OPTIFINE_DETECTED = flag;
	}

	public static final ReflectionField<Boolean> IS_SHADOW_PASS = new ReflectionField<>("net.optifine.shaders.Shaders", "isShadowPass", "isShadowPass");
	public static final ReflectionMethod<Boolean> IS_FOG_OFF = new ReflectionMethod<>("Config", "isFogOff", "isFogOff");
	public static final ReflectionField<Boolean> FOG_STANDARD = new ReflectionField<>("net.minecraft.client.renderer.EntityRenderer", "fogStandard", "fogStandard");
	public static final ReflectionMethod<Boolean> IS_SHADERS = new ReflectionMethod<>("Config", "isShaders", "isShaders");
	public static final ReflectionMethod<Boolean> IS_DYNAMIC_LIGHTS = new ReflectionMethod<>("Config", "isDynamicLights", "isDynamicLights");
	public static final ReflectionMethod<Boolean> IS_DYNAMIC_LIGHTS_FAST = new ReflectionMethod<>("Config", "isDynamicLightsFast", "isDynamicLightsFast");
	public static final ReflectionMethod<Integer> GET_LIGHT_LEVEL = new ReflectionMethod<>("net.optifine.DynamicLights", "getLightLevel", "getLightLevel", Entity.class);
	public static final ReflectionMethod<Integer> GET_COMBINED_LIGHT = new ReflectionMethod<>("net.optifine.DynamicLights", "getCombinedLight", "getCombinedLight", BlockPos.class, int.class);
	public static final ReflectionMethod<Void> DYNAMIC_LIGHTS_UPDATE = new ReflectionMethod<>("net.optifine.DynamicLights", "update", "update", RenderGlobal.class);
	public static final ReflectionMethod<Void> PRE_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "preRenderChunkLayer", "preRenderChunkLayer", BlockRenderLayer.class);
	public static final ReflectionMethod<Void> SETUP_ARRAY_POINTERS_VBO = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "setupArrayPointersVbo", "setupArrayPointersVbo");
	public static final ReflectionMethod<Void> POST_RENDER_CHUNK_LAYER = new ReflectionMethod<>("net.optifine.shaders.ShadersRender", "postRenderChunkLayer", "postRenderChunkLayer", BlockRenderLayer.class);

	public static IChunkRenderer<?> initChunkRenderer(@Nullable IChunkRenderer<?> oldChunkRenderer) {
		if (GLTest.glMultiDrawArraysIndirect && oldChunkRenderer != null) {
			if (oldChunkRenderer instanceof ChunkRendererGL43Optifine == IS_SHADERS.invoke(null)) {
				oldChunkRenderer.dispose();
				oldChunkRenderer = null;
			}
		}
		if (oldChunkRenderer == null) {
			if (GLTest.glMultiDrawArraysIndirect && !IS_SHADERS.invoke(null)) {
				return new ChunkRendererGL43Optifine();
			} else {
				return new ChunkRendererGL20Optifine();
			}
		}
		return oldChunkRenderer;
	}

}
