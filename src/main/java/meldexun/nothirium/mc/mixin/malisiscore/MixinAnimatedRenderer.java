package meldexun.nothirium.mc.mixin.malisiscore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.IBlockAccess;

@Pseudo
@Mixin(targets = "net.malisis.core.renderer.AnimatedRenderer", remap = false)
public class MixinAnimatedRenderer {

	@ModifyVariable(method = "registerRenderable", remap = false, at = @At("HEAD"), index = 0, ordinal = 0, name = "arg0")
	private static IBlockAccess pre_registerRenderable(IBlockAccess world) {
		return Minecraft.getMinecraft().world;
	}

}
