package meldexun.nothirium.mc.mixin.davincisvessels;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.BufferBuilder;

@Pseudo
@Mixin(targets = "com.elytradev.movingworld.client.render.MobileChunkRenderer$VBORender", remap = false)
public class MixinMobileChunkRendererVBORender {

	@Redirect(method = "compile", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BufferBuilder;color(FFFF)Lnet/minecraft/client/renderer/BufferBuilder;"))
	public BufferBuilder color(BufferBuilder buffer, float r, float g, float b, float a) {
		return buffer;
	}

}
