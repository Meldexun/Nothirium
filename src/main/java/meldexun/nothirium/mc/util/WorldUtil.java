package meldexun.nothirium.mc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class WorldUtil {

	public static World getWorld() {
		return Minecraft.getMinecraft().world;
	}

	public static boolean isChunkLoaded(int chunkX, int chunkZ) {
		World world = getWorld();
		if (world == null) {
			return false;
		}
		return isChunkLoaded(world, chunkX, chunkZ);
	}

	public static boolean isSectionLoaded(int sectionX, int sectionY, int sectionZ) {
		World world = getWorld();
		if (world == null) {
			return false;
		}
		return isSectionLoaded(world, sectionX, sectionY, sectionZ);
	}

	public static Chunk getChunk(int chunkX, int chunkZ) {
		World world = getWorld();
		if (world == null) {
			return null;
		}
		return getChunk(world, chunkX, chunkZ);
	}

	public static ExtendedBlockStorage getSection(int sectionX, int sectionY, int sectionZ) {
		World world = getWorld();
		if (world == null) {
			return null;
		}
		return getSection(world, sectionX, sectionY, sectionZ);
	}

	public static boolean isChunkLoaded(World world, int chunkX, int chunkZ) {
		return world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) != null;
	}

	public static boolean isSectionLoaded(World world, int sectionX, int sectionY, int sectionZ) {
		return isChunkLoaded(world, sectionX, sectionZ);
	}

	public static Chunk getChunk(World world, int chunkX, int chunkZ) {
		return world.getChunk(chunkX, chunkZ);
	}

	public static ExtendedBlockStorage getSection(World world, int sectionX, int sectionY, int sectionZ) {
		if (sectionY < 0 || sectionY >= 16) {
			return null;
		}
		Chunk chunk = getChunk(world, sectionX, sectionZ);
		if (chunk == null) {
			return null;
		}
		return chunk.getBlockStorageArray()[sectionY];
	}

}
