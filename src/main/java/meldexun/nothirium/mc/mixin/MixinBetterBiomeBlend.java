package meldexun.nothirium.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.nothirium.mc.renderer.chunk.RenderChunk;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@Pseudo
@Mixin(targets = "fionathemortal.betterbiomeblend.BiomeColor", remap = false)
public class MixinBetterBiomeBlend {

	@Inject(method = "getWorldFromBlockAccess", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private static void getWorldFromBlockAccess(IBlockAccess blockAccess, CallbackInfoReturnable<World> info) {
		if (blockAccess instanceof RenderChunk.ChunkCache) {
			info.setReturnValue(((RenderChunk.ChunkCache) blockAccess).getWorld());
		}
	}

}
