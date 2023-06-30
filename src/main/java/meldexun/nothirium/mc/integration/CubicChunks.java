package meldexun.nothirium.mc.integration;

import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubeProvider;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.mc.util.WorldUtil;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class CubicChunks {

	public static boolean isCubicWorld() {
		return isCubicWorld(WorldUtil.getWorld());
	}

	public static boolean isCubicWorld(World world) {
		return ((ICubicWorld) world).isCubicWorld();
	}

	public static boolean isSectionLoaded(World world, int sectionX, int sectionY, int sectionZ) {
		return ((ICubicWorld) world).getCubeCache().getLoadedCube(sectionX, sectionY, sectionZ) != null;
	}

	public static ExtendedBlockStorage getSection(World world, int sectionX, int sectionY, int sectionZ) {
		ICube cube = ((ICubicWorld) world).getCubeCache().getLoadedCube(sectionX, sectionY, sectionZ);
		if (cube == null) {
			return null;
		}
		return cube.getStorage();
	}

	public static boolean canCompile(IRenderChunk renderChunk) {
		ICubeProvider cubeProvider = ((ICubicWorld) WorldUtil.getWorld()).getCubeCache();
		for (int y = renderChunk.getSectionY() - 1; y <= renderChunk.getSectionY() + 1; y++) {
			for (int x = renderChunk.getSectionX() - 1; x <= renderChunk.getSectionX() + 1; x++) {
				for (int z = renderChunk.getSectionZ() - 1; z <= renderChunk.getSectionZ() + 1; z++) {
					if (cubeProvider.getLoadedCube(x, y, z) == null)
						return false;
				}
			}
		}
		return true;
	}

}
