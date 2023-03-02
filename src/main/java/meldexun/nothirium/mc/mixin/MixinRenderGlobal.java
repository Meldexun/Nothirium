package meldexun.nothirium.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.mc.util.BlockRenderLayerUtil;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

	/** ASM: {@link RenderGlobal#setWorldAndLoadRenderers(WorldClient)} */

	/** ASM: {@link RenderGlobal#loadRenderers()} */

	/** {@link RenderGlobal#stopChunkUpdates()} */
	@Inject(method = "stopChunkUpdates", cancellable = true, at = @At("HEAD"))
	public void stopChunkUpdates(CallbackInfo info) {
		info.cancel();
	}

	/** {@link RenderGlobal#getDebugInfoRenders()} */
	@Inject(method = "getDebugInfoRenders", cancellable = true, at = @At("HEAD"))
	public void getDebugInfoRenders(CallbackInfoReturnable<String> info) {
		info.setReturnValue("C: 0/0 (s) D: 0, L: 0, pC: 0, pU: 0, aB: 0");
	}

	/** {@link RenderGlobal#getRenderedChunks()} */
	@Inject(method = "getRenderedChunks", cancellable = true, at = @At("HEAD"))
	public void getRenderedChunks(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(ChunkRenderManager.renderedSections());
	}

	/** {@link RenderGlobal#setupTerrain(Entity, double, ICamera, int, boolean)} */
	@Inject(method = "setupTerrain", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0))
	public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo info) {
		ChunkRenderManager.setup();
		info.cancel();
	}

	/** {@link RenderGlobal#getRenderChunkOffset(BlockPos, RenderChunk, EnumFacing)} */
	@Inject(method = { "getRenderChunkOffset", "func_181562_a" }, remap = false, cancellable = true, at = @At("HEAD"))
	public void getRenderChunkOffset(CallbackInfoReturnable<RenderChunk> info) {
		info.setReturnValue(null);
	}

	/** {@link RenderGlobal#renderBlockLayer(BlockRenderLayer, double, int, Entity)} */
	@Inject(method = "renderBlockLayer", cancellable = true, at = @At("HEAD"))
	public void renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> info) {
		ChunkRenderManager.getRenderer().render(BlockRenderLayerUtil.getChunkRenderPass(blockLayerIn));
		info.setReturnValue(0);
	}

	/** {@link RenderGlobal#updateClouds()} */
	@Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;hasNoFreeRenderBuilders()Z"))
	public boolean hasNoFreeRenderBuilders(ChunkRenderDispatcher chunkRenderDispatcher) {
		return false;
	}

	/** {@link RenderGlobal#updateChunks(long)} */
	@Inject(method = "updateChunks", cancellable = true, at = @At("HEAD"))
	public void updateChunks(long finishTimeNano, CallbackInfo info) {
		info.cancel();
	}

	/** {@link RenderGlobal#markBlocksForUpdate(int, int, int, int, int, int, boolean)} */
	@Inject(method = "markBlocksForUpdate", cancellable = true, at = @At("HEAD"))
	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately, CallbackInfo info) {
		for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
			for (int chunkY = minY >> 4; chunkY <= maxY >> 4; chunkY++) {
				for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
					ChunkRenderManager.getProvider().setDirty(chunkX, chunkY, chunkZ);
				}
			}
		}

		info.cancel();
	}

	/** {@link RenderGlobal#hasNoChunkUpdates()} */
	@Inject(method = "hasNoChunkUpdates", cancellable = true, at = @At("HEAD"))
	public void hasNoChunkUpdates(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
	}

}
