package meldexun.nothirium.mc.util;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import net.minecraft.util.BlockRenderLayer;

public class BlockRenderLayerUtil {

	public static final BlockRenderLayer[] ALL = BlockRenderLayer.values();

	public static BlockRenderLayer getBlockRenderLayer(ChunkRenderPass pass) {
		return ALL[pass.ordinal()];
	}

	public static ChunkRenderPass getChunkRenderPass(BlockRenderLayer layer) {
		return ChunkRenderPass.ALL[layer.ordinal()];
	}

}
