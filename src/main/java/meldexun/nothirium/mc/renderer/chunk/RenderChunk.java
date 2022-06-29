package meldexun.nothirium.mc.renderer.chunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.ChunkAnimator;
import meldexun.nothirium.mc.util.EnumFacingUtil;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import meldexun.nothirium.util.Axis;
import meldexun.nothirium.util.Direction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class RenderChunk extends AbstractRenderChunk<RenderChunk> {

	public RenderChunk(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public boolean setCoords(int x, int y, int z) {
		boolean coordsUpdated = super.setCoords(x, y, z);
		if (coordsUpdated && Nothirium.isChunkAnimatorInstalled) {
			ChunkAnimator.onSetCoords(this);
		}
		return coordsUpdated;
	}

	@Override
	public void markDirty() {
		if (this.getY() >= 0 && this.getY() < 256) {
			super.markDirty();
		}
	}

	@Override
	public boolean isFaceCulled(double cameraX, double cameraY, double cameraZ, Direction direction) {
		if (direction.getAxis() == Axis.Y) {
			if (direction == Direction.UP) {
				if (this.getY() == 240 && cameraY < 256.0D)
					return true;
			} else {
				if (this.getY() == 0 && cameraY > 0.0D)
					return true;
			}
		}
		return super.isFaceCulled(cameraX, cameraY, cameraZ, direction);
	}

	@Override
	@Nullable
	public RenderChunkTaskCompile createCompileTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		if (this.getY() < 0 || this.getY() >= 256) {
			return null;
		}
		Minecraft mc = Minecraft.getMinecraft();
		Chunk chunk = mc.world.getChunk(this.getX() >> 4, this.getZ() >> 4);
		if (chunk.isEmpty()) {
			return null;
		}
		ExtendedBlockStorage blockStorage = chunk.getBlockStorageArray()[this.getY() >> 4];
		if (blockStorage == null || blockStorage.isEmpty()) {
			return null;
		}
		return new RenderChunkTaskCompile(chunkRenderer, taskDispatcher, this, new ChunkCache(mc.world, this.getX() >> 4, this.getY() >> 4, this.getZ() >> 4, 1));
	}

	@Override
	@Nullable
	protected RenderChunkTaskSortTranslucent createSortTranslucentTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		IVBOPart vboPart = this.getVBOPart(ChunkRenderPass.TRANSLUCENT);
		if (vboPart == null) {
			return null;
		}
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vboPart.getSize()).order(ByteOrder.nativeOrder());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
		GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getOffset(), byteBuffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return new RenderChunkTaskSortTranslucent(chunkRenderer, taskDispatcher, this, vboPart, byteBuffer);
	}

	public static class ChunkCache implements IBlockAccess {

		protected final World world;
		protected final int chunkX;
		protected final int chunkY;
		protected final int chunkZ;
		protected final int radius;
		protected final int gridSize;
		protected final int minX;
		protected final int maxX;
		protected final int minY;
		protected final int maxY;
		protected final int minZ;
		protected final int maxZ;
		protected final Chunk[] chunks;
		protected final ExtendedBlockStorage[] sections;

		public ChunkCache(World world, int chunkX, int chunkY, int chunkZ, int radius) {
			this.world = world;
			this.chunkX = chunkX;
			this.chunkY = chunkY;
			this.chunkZ = chunkZ;
			this.radius = radius;
			this.gridSize = radius * 2 + 1;
			this.minX = (chunkX - radius) << 4;
			this.maxX = ((chunkX + radius) << 4) + 15;
			this.minY = (chunkY - radius) << 4;
			this.maxY = ((chunkY + radius) << 4) + 15;
			this.minZ = (chunkZ - radius) << 4;
			this.maxZ = ((chunkZ + radius) << 4) + 15;
			this.chunks = new Chunk[gridSize * gridSize];
			this.sections = new ExtendedBlockStorage[gridSize * gridSize * gridSize];

			for (int x = chunkX - radius; x <= chunkX + radius; x++) {
				for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
					Chunk chunk = world.getChunkProvider().getLoadedChunk(x, z);
					if (chunk == null)
						continue;
					this.chunks[chunkIndex(x, z)] = chunk;
					for (int y = chunkY - radius; y <= chunkY + radius; y++) {
						ExtendedBlockStorage blockStorage = y >= 0 && y < 16 ? chunk.getBlockStorageArray()[y] : null;
						this.sections[sectionIndex(x, y, z)] = blockStorage;
					}
				}
			}
		}

		private int chunkIndex(BlockPos pos) {
			return chunkIndex(pos.getX() >> 4, pos.getZ() >> 4);
		}

		private int chunkIndex(int chunkX, int chunkZ) {
			return (chunkZ - this.chunkZ + radius) * gridSize + (chunkX - this.chunkX + radius);
		}

		private int sectionIndex(BlockPos pos) {
			return sectionIndex(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		}

		private int sectionIndex(int chunkX, int chunkY, int chunkZ) {
			return ((chunkZ - this.chunkZ + radius) * gridSize + (chunkY - this.chunkY + radius)) * gridSize + (chunkX - this.chunkX + radius);
		}

		private boolean withinBoundsXZ(BlockPos pos) {
			return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
		}

		private boolean withinBoundsY(BlockPos pos) {
			return pos.getY() >= minY && pos.getY() <= maxY;
		}

		private boolean withinBoundsXYZ(BlockPos pos) {
			return withinBoundsXZ(pos) && withinBoundsY(pos);
		}

		@Nullable
		private Chunk getChunk(BlockPos pos) {
			return this.chunks[chunkIndex(pos)];
		}

		@Nullable
		private ExtendedBlockStorage getBlockStorage(BlockPos pos) {
			return this.sections[sectionIndex(pos)];
		}

		@Override
		@Nullable
		public TileEntity getTileEntity(BlockPos pos) {
			if (!withinBoundsXYZ(pos)) {
				return null;
			}
			Chunk chunk = this.getChunk(pos);
			if (chunk == null) {
				return null;
			}
			return chunk.getTileEntity(pos, EnumCreateEntityType.CHECK);
		}

		@Override
		public int getCombinedLight(BlockPos pos, int minBlockLight) {
			IBlockState state = this.getBlockState(pos);

			if (state.useNeighborBrightness()) {
				MutableBlockPos mutable = new MutableBlockPos();
				int light = minBlockLight << 4;

				for (EnumFacing facing : EnumFacingUtil.ALL) {
					if (state.doesSideBlockRendering(this, pos, facing)) {
						continue;
					}
					light = getSkyBlockLight(mutable.setPos(pos).move(facing), light);
					if (light == (this.world.provider.hasSkyLight() ? 0xF0F0 : 0xF0)) {
						break;
					}
				}

				return light;
			} else {
				return getSkyBlockLight(pos, minBlockLight << 4);
			}
		}

		public int getSkyBlockLight(BlockPos pos, int minCombinedLight) {
			int sky = (minCombinedLight >> 20) & 15;
			int block = (minCombinedLight >> 4) & 15;
			ExtendedBlockStorage blockStorage;
			Chunk chunk;

			boolean withinBoundsXZ = withinBoundsXZ(pos);
			if (withinBoundsXZ && withinBoundsY(pos) && (blockStorage = this.getBlockStorage(pos)) != null) {
				if (this.world.provider.hasSkyLight() && sky < 15) {
					sky = Math.max(blockStorage.getSkyLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), sky);
				}
				if (block < 15) {
					block = Math.max(blockStorage.getBlockLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), block);
				}
			} else if (this.world.provider.hasSkyLight() && sky < 15 && (!withinBoundsXZ || (chunk = this.getChunk(pos)) == null || chunk.canSeeSky(pos))) {
				sky = 15;
			}

			return sky << 20 | block << 4;
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			if (!withinBoundsXYZ(pos)) {
				return Blocks.AIR.getDefaultState();
			}
			ExtendedBlockStorage blockStorage = this.getBlockStorage(pos);
			if (blockStorage == null) {
				return Blocks.AIR.getDefaultState();
			}
			return blockStorage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			IBlockState state = this.getBlockState(pos);
			return state.getBlock().isAir(state, this, pos);
		}

		@Override
		public Biome getBiome(BlockPos pos) {
			if (!withinBoundsXZ(pos)) {
				return Biomes.PLAINS;
			}
			Chunk chunk = this.getChunk(pos);
			if (chunk == null) {
				return Biomes.PLAINS;
			}
			return chunk.getBiome(pos, this.world.getBiomeProvider());
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return this.getBlockState(pos).getStrongPower(this, pos, direction);
		}

		@Override
		public WorldType getWorldType() {
			return world.getWorldType();
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			if (!this.withinBoundsXYZ(pos)) {
				return _default;
			}
			return this.getBlockState(pos).isSideSolid(this, pos, side);
		}

		public World getWorld() {
			return this.world;
		}

	}

}
