package meldexun.nothirium.mc.util;

import meldexun.nothirium.util.Direction;
import net.minecraft.util.EnumFacing;

public class EnumFacingUtil {

	public static final EnumFacing[] ALL = EnumFacing.VALUES;

	public static EnumFacing getFacing(Direction dir) {
		return ALL[dir.ordinal()];
	}

	public static Direction getDirection(EnumFacing facing) {
		return Direction.ALL[facing.ordinal()];
	}

}
