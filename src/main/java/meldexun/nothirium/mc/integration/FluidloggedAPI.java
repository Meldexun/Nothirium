package meldexun.nothirium.mc.integration;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile;
import meldexun.nothirium.util.VisibilityGraph;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FluidloggedAPI {

	public static void renderFluidState(RenderChunkTaskCompile chunkCompiler, IBlockAccess world, BlockPos pos,
			IBlockState blockState, VisibilityGraph visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack) {
		if (FluidloggedUtils.isFluid(blockState)) {
			return;
		}
		FluidState fluidState = FluidState.get(world, pos);
		if (fluidState.isEmpty()) {
			return;
		}
		if (blockState.getBlock() instanceof IFluidloggable
				&& !((IFluidloggable) blockState.getBlock()).shouldFluidRender(world, pos, blockState, fluidState)) {
			return;
		}
		chunkCompiler.renderBlockState(fluidState.getState(), pos, visibilityGraph, bufferBuilderPack);
	}

}
