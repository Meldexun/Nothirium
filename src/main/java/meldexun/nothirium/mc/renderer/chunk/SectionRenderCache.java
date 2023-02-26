package meldexun.nothirium.mc.renderer.chunk;

import java.util.stream.IntStream;

import javax.annotation.Nullable;

import meldexun.nothirium.mc.integration.Optifine;
import meldexun.nothirium.mc.util.LightUtil;
import meldexun.nothirium.util.SectionPos;
import meldexun.nothirium.util.cache.Cache2D;
import meldexun.nothirium.util.cache.Cache3D;
import meldexun.nothirium.util.cache.IntCache3D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class SectionRenderCache implements IBlockAccess {

	protected final World world;
	protected final SectionPos sectionPos;
	protected final Cache2D<Chunk> chunkCache;
	protected final Cache3D<ExtendedBlockStorage> sectionCache;
	protected final Cache3D<IBlockState> blockCache;
	protected final IntCache3D lightCache;
	protected final Cache2D<Biome> biomeCache;

	public SectionRenderCache(World world, SectionPos sectionPos, int radius) {
		this.world = world;
		this.sectionPos = sectionPos;
		int minX = sectionPos.getBlockX() - radius;
		int minY = sectionPos.getBlockY() - radius;
		int minZ = sectionPos.getBlockZ() - radius;
		int maxX = sectionPos.getBlockX() + 15 + radius;
		int maxY = sectionPos.getBlockY() + 15 + radius;
		int maxZ = sectionPos.getBlockZ() + 15 + radius;
		int minChunkX = minX >> 4;
		int minChunkY = minY >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkX = maxX >> 4;
		int maxChunkY = maxY >> 4;
		int maxChunkZ = maxZ >> 4;
		this.chunkCache = new Cache2D<>(minChunkX, minChunkZ, maxChunkX, maxChunkZ, null, Chunk[]::new);
		this.sectionCache = new Cache3D<>(minChunkX, minChunkY, minChunkZ, maxChunkX, maxChunkY, maxChunkZ, null, ExtendedBlockStorage[]::new);
		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				Chunk chunk = this.chunkCache.computeIfAbsent(x, z, world::getChunk);
				for (int y = minChunkY; y <= maxChunkY; y++) {
					this.sectionCache.computeIfAbsent(x, y, z, (chunkX, chunkY, chunkZ) -> {
						if (chunkY < 0 || chunkY >= 16) {
							return null;
						}
						return chunk.getBlockStorageArray()[chunkY];
					});
				}
			}
		}
		this.blockCache = new Cache3D<>(minX, minY, minZ, maxX, maxY, maxZ, Blocks.AIR.getDefaultState(), IBlockState[]::new);
		this.lightCache = new IntCache3D(minX, minY, minZ, maxX, maxY, maxZ, 0, size -> IntStream.range(0, size).map(i -> -1).toArray());
		this.biomeCache = new Cache2D<>(minX, minZ, maxX, maxZ, Biomes.PLAINS, Biome[]::new);
	}

	@Nullable
	private Chunk getChunk(BlockPos pos) {
		return this.chunkCache.get(pos.getX() >> 4, pos.getZ() >> 4);
	}

	@Nullable
	private ExtendedBlockStorage getSection(BlockPos pos) {
		return this.sectionCache.get(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
	}

	@Override
	@Nullable
	public TileEntity getTileEntity(BlockPos pos) {
		Chunk chunk = this.getChunk(pos);
		return chunk == null ? null : chunk.getTileEntity(pos, EnumCreateEntityType.CHECK);
	}

	@Override
	public int getCombinedLight(BlockPos pos, int minBlockLight) {
		return this.lightCache.compute(pos, (p, l) -> l == -1 ? this.calculateCombinedLight(p, minBlockLight) : l);
	}

	private int calculateCombinedLight(BlockPos pos, int minBlockLight) {
		int light = LightUtil.pack(0, minBlockLight);
		IBlockState state = this.getBlockState(pos);

		if (state.useNeighborBrightness()) {
			MutableBlockPos mutable = new MutableBlockPos();
			light = this.getLight(mutable.setPos(pos.getX(), pos.getY() - 1, pos.getZ()), light);
			light = this.getLight(mutable.setPos(pos.getX(), pos.getY() + 1, pos.getZ()), light);
			light = this.getLight(mutable.setPos(pos.getX(), pos.getY(), pos.getZ() - 1), light);
			light = this.getLight(mutable.setPos(pos.getX(), pos.getY(), pos.getZ() + 1), light);
			light = this.getLight(mutable.setPos(pos.getX() - 1, pos.getY(), pos.getZ()), light);
			light = this.getLight(mutable.setPos(pos.getX() + 1, pos.getY(), pos.getZ()), light);
		} else {
			light = this.getLight(pos, light);
		}

		if (Optifine.OPTIFINE_DETECTED && Optifine.IS_DYNAMIC_LIGHTS.invoke(null) && !state.isOpaqueCube()) {
			light = Optifine.GET_COMBINED_LIGHT.invoke(null, pos, light);
		}

		return light;
	}

	private int getLight(BlockPos pos, int skyBlock) {
		return this.getLight(pos, LightUtil.sky(skyBlock), LightUtil.block(skyBlock));
	}

	private int getLight(BlockPos pos, int sky, int block) {
		ExtendedBlockStorage section = this.getSection(pos);
		if (section != null) {
			if (this.world.provider.hasSkyLight() && sky < 15) {
				sky = Math.max(sky, LightUtil.getSkyLight(section, pos));
			}
			if (block < 15) {
				block = Math.max(block, LightUtil.getBlockLight(section, pos));
			}
		} else if (this.world.provider.hasSkyLight() && sky < EnumSkyBlock.SKY.defaultLightValue) {
			Chunk chunk = this.getChunk(pos);
			if (chunk != null && chunk.canSeeSky(pos)) {
				sky = EnumSkyBlock.SKY.defaultLightValue;
			}
		}
		return LightUtil.pack(sky, block);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return this.blockCache.computeIfAbsent(pos, p -> {
			ExtendedBlockStorage section = this.getSection(p);
			return section == null ? Blocks.AIR.getDefaultState()
					: section.get(p.getX() & 15, p.getY() & 15, p.getZ() & 15);
		});
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IBlockState state = this.getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return this.biomeCache.computeIfAbsent(pos, p -> {
			Chunk chunk = this.getChunk(p);
			return chunk == null ? Biomes.PLAINS : chunk.getBiome(p, this.world.getBiomeProvider());
		});
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return this.getBlockState(pos).getStrongPower(this, pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		if (!this.blockCache.inBounds(pos.getX(), pos.getY(), pos.getZ())) {
			return _default;
		}
		return this.getBlockState(pos).isSideSolid(this, pos, side);
	}

	public World getWorld() {
		return this.world;
	}

}
