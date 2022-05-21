package meldexun.nothirium.mc.renderer.chunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import meldexun.nothirium.util.Axis;
import meldexun.nothirium.util.Direction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
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
		return new RenderChunkTaskCompile(chunkRenderer, taskDispatcher, this, new ChunkCache(mc.world, this.getX() >> 4, this.getY() >> 4, this.getZ() >> 4));
	}

	@Override
	@Nullable
	protected RenderChunkTaskSortTranslucent createSortTranslucentTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		IVBOPart vboPart = this.getVBOPart(ChunkRenderPass.TRANSLUCENT);
		if (vboPart == null) {
			return null;
		}
		int vertexSize = DefaultVertexFormats.BLOCK.getSize();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vboPart.getCount() * vertexSize).order(ByteOrder.nativeOrder());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
		GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getFirst() * vertexSize, byteBuffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return new RenderChunkTaskSortTranslucent(chunkRenderer, taskDispatcher, this, vboPart, byteBuffer);
	}

	public static class ChunkCache implements IBlockAccess {

		protected final World world;
		protected final int chunkX;
		protected final int chunkY;
		protected final int chunkZ;
		protected final Chunk[] chunks = new Chunk[9];
		protected final ExtendedBlockStorage[] blockStorages = new ExtendedBlockStorage[27];

		public ChunkCache(World world, int chunkX, int chunkY, int chunkZ) {
			this.world = world;
			this.chunkX = chunkX;
			this.chunkY = chunkY;
			this.chunkZ = chunkZ;

			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					if (x != 0 && z != 0) {
						continue;
					}
					Chunk chunk = world.getChunk(chunkX + x, chunkZ + z);
					this.chunks[(z + 1) * 3 + (x + 1)] = chunk;
					for (int y = -1; y <= 1; y++) {
						if ((x != 0 || z != 0) && y != 0) {
							continue;
						}
						ExtendedBlockStorage blockStorage = chunkY + y >= 0 && chunkY + y < 16 ? chunk.getBlockStorageArray()[chunkY + y] : null;
						this.blockStorages[((z + 1) * 3 + (y + 1)) * 3 + (x + 1)] = blockStorage;
					}
				}
			}
		}

		private boolean withinBounds(int x, int y, int z) {
			return (x >= -1 && x <= 1) && (y >= -1 && y <= 1) && (z >= -1 && z <= 1);
		}

		@Nullable
		private Chunk getChunk(BlockPos pos) {
			int x = (pos.getX() >> 4) - this.chunkX;
			int y = (pos.getY() >> 4) - this.chunkY;
			int z = (pos.getZ() >> 4) - this.chunkZ;
			if (!withinBounds(x, y, z)) {
				return null;
			}
			return this.chunks[(z + 1) * 3 + (x + 1)];
		}

		@Nullable
		private ExtendedBlockStorage getBlockStorage(BlockPos pos) {
			int x = (pos.getX() >> 4) - this.chunkX;
			int y = (pos.getY() >> 4) - this.chunkY;
			int z = (pos.getZ() >> 4) - this.chunkZ;
			if (!withinBounds(x, y, z)) {
				return null;
			}
			return this.blockStorages[((z + 1) * 3 + (y + 1)) * 3 + (x + 1)];
		}

		@Override
		@Nullable
		public TileEntity getTileEntity(BlockPos pos) {
			Chunk chunk = this.getChunk(pos);
			if (chunk == null) {
				return null;
			}
			return chunk.getTileEntity(pos, EnumCreateEntityType.CHECK);
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			Chunk chunk = this.getChunk(pos);
			if (chunk == null) {
				return this.world.provider.hasSkyLight() ? EnumSkyBlock.SKY.defaultLightValue << 20 : 0;
			}
			ExtendedBlockStorage blockStorage = this.getBlockStorage(pos);
			if (blockStorage == null) {
				return this.world.provider.hasSkyLight() && chunk.canSeeSky(pos) ? EnumSkyBlock.SKY.defaultLightValue << 20 : 0;
			}
			int skyLight = this.world.provider.hasSkyLight() ? blockStorage.getSkyLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15) : 0;
			int blockLight = blockStorage.getBlockLight(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
			return skyLight << 20 | blockLight << 4;
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
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
			if (!this.withinBounds((pos.getX() >> 4) - chunkX, (pos.getY() >> 4) - chunkY, (pos.getZ() >> 4) - chunkZ)) {
				return _default;
			}
			return this.getBlockState(pos).isSideSolid(this, pos, side);
		}

	}

}
