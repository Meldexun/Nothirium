package meldexun.nothirium.mc.mixin.optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

	/** {@link EntityRenderer#setupCameraTransform(float, int)} */
	@Redirect(method = "setupCameraTransform", require = 0, at = @At(value = "INVOKE", target = "LConfig;isFogFancy()Z"))
	public boolean isFogFancy(float partialTicks, int pass) {
		return false;
	}

	/** {@link EntityRenderer#setupCameraTransform(float, int)} */
	@Redirect(method = "setupCameraTransform", require = 0, at = @At(value = "INVOKE", target = "LConfig;isFogFast()Z"))
	public boolean isFogFast(float partialTicks, int pass) {
		return false;
	}

}
