package meldexun.nothirium.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class SectionPos {

	private final int x;
	private final int y;
	private final int z;

	private SectionPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static SectionPos of(BlockPos pos) {
		return of(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
	}

	public static SectionPos of(ChunkPos chunkPos, int y) {
		return of(chunkPos.x, y, chunkPos.z);
	}

	public static SectionPos of(int x, int y, int z) {
		return new SectionPos(x, y, z);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int getBlockX() {
		return x << 4;
	}

	public int getBlockY() {
		return y << 4;
	}

	public int getBlockZ() {
		return z << 4;
	}

}
