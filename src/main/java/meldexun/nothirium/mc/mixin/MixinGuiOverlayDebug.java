package meldexun.nothirium.mc.mixin;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;

@Mixin(GuiOverlayDebug.class)
public class MixinGuiOverlayDebug {

	@Shadow
	private Minecraft mc;

	@Inject(method = "call", at = @At("RETURN"))
	public void call(CallbackInfoReturnable<List<String>> info) {
		List<String> list = info.getReturnValue();
		search(list, s -> s.contains("fps"), i -> {
			list.add(i + 1, ChunkRenderManager.getRenderer().name());
		});
		search(list, s -> s.startsWith("C:"), i -> {
			list.set(i, "Chunks:");
			list.add(i + 1, String.format("  Solid: %d", ChunkRenderManager.renderedSections(ChunkRenderPass.SOLID)));
			list.add(i + 2, String.format("  Cutout: %d", ChunkRenderManager.renderedSections(ChunkRenderPass.CUTOUT)));
			list.add(i + 3, String.format("  Cutout Mipped: %d", ChunkRenderManager.renderedSections(ChunkRenderPass.CUTOUT_MIPPED)));
			list.add(i + 4, String.format("  Translucent: %d", ChunkRenderManager.renderedSections(ChunkRenderPass.TRANSLUCENT)));
			list.add(i + 5, String.format("  Total: %d", ChunkRenderManager.totalSections()));
		});
	}

	private static void search(List<String> list, Predicate<String> predicate, IntConsumer action) {
		for (int i = 0; i < list.size(); i++) {
			if (predicate.test(list.get(i))) {
				action.accept(i);
				return;
			}
		}
	}

}
