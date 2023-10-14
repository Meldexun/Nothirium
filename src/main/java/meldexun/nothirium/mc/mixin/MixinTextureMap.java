package meldexun.nothirium.mc.mixin;

import meldexun.nothirium.mc.integration.CensoredASM;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TextureMap.class, priority = 1100)
public class MixinTextureMap {
    @Inject(method = "updateAnimations", at = @At("HEAD"))
    private void triggerChunkSprites(CallbackInfo ci) {
        if(((TextureMap)(Object)this) == Minecraft.getMinecraft().getTextureMapBlocks()) {
            CensoredASM.activateChunkSprites();
        }
    }
}
