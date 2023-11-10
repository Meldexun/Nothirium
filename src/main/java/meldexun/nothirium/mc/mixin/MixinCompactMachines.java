package meldexun.nothirium.mc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.CubeTools;
import org.dave.compactmachines3.reference.EnumMachineSize;

@Mixin(value = CubeTools.class, remap = false)
public class MixinCompactMachines {

	@Unique
	private static int nothirium$getCubeSizeWithYContext(IBlockAccess section, MutableBlockPos pos) {
		pos.setPos(pos.getX() * 1024, pos.getY(), pos.getZ());
		for (int i = EnumMachineSize.values().length - 1; i >= 0; i--) {
			EnumMachineSize size = EnumMachineSize.values()[i];
			// (x + dimension, y, z)
			pos.move(EnumFacing.EAST, size.getDimension());
			if (section.getBlockState(pos).getBlock() == Blockss.wall) {
				return size.getDimension();
			}
			pos.move(EnumFacing.WEST, size.getDimension());
		}
		return EnumMachineSize.TINY.getDimension();
	}

	@Redirect(method = "shouldSideBeRendered", at = @At(value = "INVOKE", target = "Lorg/dave/compactmachines3/reference/EnumMachineSize;getDimension()I"))
	private static int reassignSize(EnumMachineSize instance, IBlockAccess world, BlockPos pos) {
		PooledMutableBlockPos mPos = PooledMutableBlockPos.retain(pos);
		try {
			return nothirium$getCubeSizeWithYContext(world, mPos.setPos(pos.getX() / 1024, pos.getY(), 0));
		} finally {
			// Have to manually release in 1.12
			mPos.release();
		}
	}

}
