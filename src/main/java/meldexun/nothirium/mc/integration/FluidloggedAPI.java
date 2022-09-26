package meldexun.nothirium.mc.integration;

import java.util.function.Consumer;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FluidloggedAPI {

	public static void renderFluidState(IBlockState blockState, IBlockAccess world, BlockPos pos, Consumer<IBlockState> renderer) {
		if (FluidloggedUtils.getFluidFromState(blockState) == null) {
			FluidState fluidState = FluidState.get(pos);
			if (!fluidState.isEmpty() && shouldRender(blockState, world, pos, fluidState)) {
				renderer.accept(fluidState.getState());
			}
		}
	}

	private static boolean shouldRender(IBlockState blockState, IBlockAccess world, BlockPos pos, FluidState fluidState) {
		return !(blockState.getBlock() instanceof IFluidloggable) || ((IFluidloggable) blockState.getBlock()).shouldFluidRender(world, pos, blockState, fluidState);
	}

}
