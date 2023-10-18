package meldexun.nothirium.mc.integration;

import com.google.common.collect.ImmutableSet;
import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.mc.vertex.ExtendedBufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.common.Loader;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpriteActivator;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.IBufferPrimerConfigurator;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;

import java.util.Set;

public class CensoredASM {
    // Simple hack to check if onDemandAnimatedTextures is enabled
    private static final boolean LOADED = Loader.isModLoaded("loliasm") && IAnimatedSpriteActivator.class.isAssignableFrom(TextureAtlasSprite.class);

    public static void startCapturingChunkTextures() {
        if(!LOADED)
            return;
        IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.set(new CompiledChunk());
    }

    public static Set<TextureAtlasSprite> stopCapturingChunkTextures() {
        if(!LOADED)
            return ImmutableSet.of();
        CompiledChunk c = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
        if(c instanceof ICompiledChunkExpander)
            return ((ICompiledChunkExpander)c).getVisibleTextures();
        else
            return ImmutableSet.of();
    }


    public static void triggerSpriteOnBufferAccess(ExtendedBufferBuilder builder, float u, float v) {
        if(LOADED && builder instanceof IBufferPrimerConfigurator) {
            try {
                ((IBufferPrimerConfigurator)builder).hookTexture(u, v);
            } catch(Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void activateChunkSprites() {
        if(!LOADED)
            return;
        for(IRenderChunk c : ChunkRenderManager.getRenderer().getRenderChunks()) {
            for(TextureAtlasSprite sprite : c.getVisibleTextures()) {
                ((IAnimatedSpriteActivator)sprite).setActive(true);
            }
        }
    }
}
