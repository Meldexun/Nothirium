package meldexun.nothirium.mc.mixin;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.renderlib.renderer.EntityRenderManager;
import meldexun.renderlib.renderer.TileEntityRenderManager;
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
			list.set(i, String.format("Chunks: %d/%d", ChunkRenderManager.renderedSections(), ChunkRenderManager.totalSections()));
		});
		search(list, s -> s.startsWith("E:"), i -> {
			list.set(i, String.format("Entities: %d/%d", EntityRenderManager.renderedEntities(), EntityRenderManager.totalEntities()));
			list.add(i + 1, String.format("Tile Entities: %d/%d", TileEntityRenderManager.renderedTileEntities(), TileEntityRenderManager.totalTileEntities()));
		});
		search(list, s -> s.startsWith("P:"), i -> {
			list.set(i, String.format("Particles: %s", this.mc.effectRenderer.getStatistics()));
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
