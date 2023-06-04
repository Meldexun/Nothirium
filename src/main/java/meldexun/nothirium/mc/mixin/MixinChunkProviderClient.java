package meldexun.nothirium.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;

@Mixin(ChunkProviderClient.class)
public class MixinChunkProviderClient {

	/** {@link ChunkProviderClient#loadChunk(int, int)} */
	@Inject(method = "loadChunk", at = @At("RETURN"))
	public void loadChunk(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> info) {
		if (ChunkRenderManager.getProvider() != null) {
			ChunkRenderManager.getProvider().setLoaded(chunkX, chunkZ, true);
		}
	}

	/** {@link ChunkProviderClient#unloadChunk(int, int)} */
	@Inject(method = "unloadChunk", at = @At("RETURN"))
	public void unloadChunk(int chunkX, int chunkZ, CallbackInfo info) {
		if (ChunkRenderManager.getProvider() != null) {
			ChunkRenderManager.getProvider().setLoaded(chunkX, chunkZ, false);
		}
	}

}
