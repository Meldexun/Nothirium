package meldexun.nothirium.mc.integration;

import meldexun.reflectionutil.ReflectionMethod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BetterFoliage {

	private static final ReflectionMethod<Boolean> CAN_RENDER_BLOCK_IN_LAYER = new ReflectionMethod<>(
			"mods.betterfoliage.client.Hooks", "canRenderBlockInLayer", "canRenderBlockInLayer", Block.class,
			IBlockState.class, BlockRenderLayer.class);
	private static final ReflectionMethod<Boolean> RENDER_WORLD_BLOCK = new ReflectionMethod<>(
			"mods.betterfoliage.client.Hooks", "renderWorldBlock", "renderWorldBlock", BlockRendererDispatcher.class,
			IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class, BlockRenderLayer.class);

	public static boolean canRenderBlockInLayer(Block block, IBlockState state, BlockRenderLayer layer) {
		return CAN_RENDER_BLOCK_IN_LAYER.invoke(null, block, state, layer);
	}

	public static boolean renderWorldBlock(BlockRendererDispatcher blockRenderer, IBlockState state, BlockPos pos,
			IBlockAccess world, BufferBuilder buffer, BlockRenderLayer layer) {
		return RENDER_WORLD_BLOCK.invoke(null, blockRenderer, state, pos, world, buffer, layer);
	}

}
