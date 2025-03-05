package meldexun.nothirium.mc.renderer.chunk;

import java.util.function.Function;

import javax.annotation.Nullable;

import meldexun.nothirium.mc.util.LightUtil;
import meldexun.nothirium.mc.util.WorldUtil;
import meldexun.nothirium.util.SectionPos;
import meldexun.nothirium.util.cache.ArrayCache;
import meldexun.nothirium.util.cache.Cache2D;
import meldexun.nothirium.util.cache.Cache3D;
import meldexun.nothirium.util.cache.IntArrayCache;
import meldexun.nothirium.util.cache.IntCache3D;
import meldexun.nothirium.util.function.ObjInt2IntFunction;
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

	private static final ArrayCache<IBlockState> BLOCK = new ArrayCache<>(18 * 18 * 18, IBlockState[]::new, null);
	private static final IntArrayCache LIGHT = new IntArrayCache(18 * 18 * 18, -1);
	private static final ArrayCache<Biome> BIOME = new ArrayCache<>(48 * 48, Biome[]::new, null);
	protected final World world;
	protected final SectionPos sectionPos;
	protected final Cache2D<Chunk> chunkCache;
	protected final Cache3D<ExtendedBlockStorage> sectionCache;
	protected Cache3D<IBlockState> blockCache;
	protected IntCache3D lightCache;
	protected Cache2D<Biome> biomeCache;

	protected final Function<BlockPos, IBlockState> calculateBlockStateRef = this::calculateBlockState;
	protected int minBlockLight;
	protected final ObjInt2IntFunction<BlockPos> calculateCombinedLightRef = (pos, value) -> {
		return value == -1 ? calculateCombinedLight(pos, this.minBlockLight) : value;
	};
	protected final Function<BlockPos, Biome> calculateBiomeRef = this::calculateBiome;

	public SectionRenderCache(World world, SectionPos sectionPos) {
		this.world = world;
		this.sectionPos = sectionPos;
		int minChunkX = sectionPos.getX() - 1;
		int minChunkY = sectionPos.getY() - 1;
		int minChunkZ = sectionPos.getZ() - 1;
		int maxChunkX = sectionPos.getX() + 1;
		int maxChunkY = sectionPos.getY() + 1;
		int maxChunkZ = sectionPos.getZ() + 1;
		this.chunkCache = new Cache2D<>(minChunkX, minChunkZ, maxChunkX, maxChunkZ, null, Chunk[]::new);
		this.sectionCache = new Cache3D<>(minChunkX, minChunkY, minChunkZ, maxChunkX, maxChunkY, maxChunkZ, null, ExtendedBlockStorage[]::new);
		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				this.chunkCache.computeIfAbsent(x, z, world::getChunk);
				for (int y = minChunkY; y <= maxChunkY; y++) {
					this.sectionCache.computeIfAbsent(x, y, z, WorldUtil::getSection);
				}
			}
		}
	}

	public void initCaches() {
		int minX = sectionPos.getBlockX();
		int minY = sectionPos.getBlockY();
		int minZ = sectionPos.getBlockZ();
		int maxX = sectionPos.getBlockX() + 15;
		int maxY = sectionPos.getBlockY() + 15;
		int maxZ = sectionPos.getBlockZ() + 15;
		this.blockCache = new Cache3D<>(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1, Blocks.AIR.getDefaultState(), BLOCK.get());
		this.lightCache = new IntCache3D(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1, 0, LIGHT.get());
		this.biomeCache = new Cache2D<>(minX - 16, minZ - 16, maxX + 16, maxZ + 16, Biomes.PLAINS, BIOME.get());
	}

	public void freeCaches() {
		BLOCK.free(blockCache.getData());
		LIGHT.free(lightCache.getData());
		BIOME.free(biomeCache.getData());
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
		this.minBlockLight = minBlockLight;
		return this.lightCache.compute(pos, this.calculateCombinedLightRef);
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
		return this.blockCache.computeIfAbsent(pos, this.calculateBlockStateRef);
	}

	private IBlockState calculateBlockState(BlockPos pos) {
		ExtendedBlockStorage section = this.getSection(pos);
		return section == null ? Blocks.AIR.getDefaultState()
				: section.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IBlockState state = this.getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return this.biomeCache.computeIfAbsent(pos, this.calculateBiomeRef);
	}

	private Biome calculateBiome(BlockPos pos) {
		Chunk chunk = this.getChunk(pos);
		return chunk == null ? Biomes.PLAINS : chunk.getBiome(pos, this.getWorld().getBiomeProvider());
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
