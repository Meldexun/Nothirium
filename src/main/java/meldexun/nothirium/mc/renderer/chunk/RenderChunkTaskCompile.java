package meldexun.nothirium.mc.renderer.chunk;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.BetterFoliage;
import meldexun.nothirium.mc.integration.FluidloggedAPI;
import meldexun.nothirium.mc.util.BlockRenderLayerUtil;
import meldexun.nothirium.mc.util.EnumFacingUtil;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.VisibilityGraph;
import meldexun.nothirium.util.VisibilitySet;
import meldexun.renderlib.util.BufferUtil;
import meldexun.renderlib.util.UnsafeBuffer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderChunkTaskCompile extends AbstractRenderChunkTask<RenderChunk> {

	private static final BlockingQueue<RegionRenderCacheBuilder> BUFFER_QUEUE = new LinkedBlockingQueue<>();
	static {
		IntStream.range(0, (Runtime.getRuntime().availableProcessors() - 2) * 2).mapToObj(i -> new RegionRenderCacheBuilder()).forEach(BUFFER_QUEUE::add);
	}

	private final IBlockAccess chunkCache;

	public RenderChunkTaskCompile(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher, RenderChunk renderChunk, IBlockAccess chunkCache) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.chunkCache = chunkCache;
	}

	private static void freeBuffer(RegionRenderCacheBuilder buffer) {
		for (BlockRenderLayer layer : BlockRenderLayerUtil.ALL) {
			BufferBuilder bufferBuilder = buffer.getWorldRendererByLayer(layer);
			if (bufferBuilder.isDrawing) {
				bufferBuilder.finishDrawing();
			}
			bufferBuilder.reset();
			bufferBuilder.setTranslation(0, 0, 0);
		}
		BUFFER_QUEUE.add(buffer);
	}

	@Override
	public RenderChunkTaskResult run() {
		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		RegionRenderCacheBuilder bufferBuilderPack;
		try {
			bufferBuilderPack = BUFFER_QUEUE.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return RenderChunkTaskResult.CANCELLED;
		}

		if (this.canceled()) {
			freeBuffer(bufferBuilderPack);
			return RenderChunkTaskResult.CANCELLED;
		}

		try {
			Minecraft mc = Minecraft.getMinecraft();
			MutableBlockPos pos = new MutableBlockPos();
			VisibilityGraph visibilityGraph = new VisibilityGraph();

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						pos.setPos(this.renderChunk.getX() + x, this.renderChunk.getY() + y, this.renderChunk.getZ() + z);
						IBlockState blockState = this.chunkCache.getBlockState(pos);
						renderBlockState(blockState, pos, visibilityGraph, bufferBuilderPack, mc);

						if (Nothirium.isFluidloggedAPIInstalled) {
							FluidloggedAPI.renderFluidState(blockState, this.chunkCache, pos, fluidState -> renderBlockState(fluidState, pos, visibilityGraph, bufferBuilderPack, mc));
						}
					}
				}

				if (this.canceled()) {
					freeBuffer(bufferBuilderPack);
					return RenderChunkTaskResult.CANCELLED;
				}
			}

			VisibilitySet visibilitySet = visibilityGraph.compute();

			if (bufferBuilderPack.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).isDrawing) {
				Entity entity = mc.getRenderViewEntity();
				if (entity != null) {
					BufferBuilder bufferBuilder = bufferBuilderPack
							.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT);
					RenderChunkTaskSortTranslucent.sortVertexData(renderChunk, bufferBuilder.getVertexCount() / 4,
							new UnsafeBuffer<>(bufferBuilder.getByteBuffer()), entity.getPositionEyes(1.0F));
				}
			}

			if (this.canceled()) {
				freeBuffer(bufferBuilderPack);
				return RenderChunkTaskResult.CANCELLED;
			}

			BufferBuilder[] finishedBufferBuilders = Arrays.stream(BlockRenderLayerUtil.ALL)
					.map(bufferBuilderPack::getWorldRendererByLayer)
					.map(bufferBuilder -> {
						if (!bufferBuilder.isDrawing) {
							return null;
						}
						bufferBuilder.finishDrawing();
						if (bufferBuilder.getVertexCount() == 0) {
							return null;
						}
						return bufferBuilder;
					}).toArray(BufferBuilder[]::new);

			if (this.canceled()) {
				freeBuffer(bufferBuilderPack);
				return RenderChunkTaskResult.CANCELLED;
			}

			this.taskDispatcher.runOnRenderThread(() -> {
				try {
					if (!this.canceled()) {
						this.renderChunk.setVisibility(visibilitySet);
						for (ChunkRenderPass pass : ChunkRenderPass.ALL) {
							BufferBuilder bufferBuilder = finishedBufferBuilders[pass.ordinal()];
							if (bufferBuilder == null) {
								this.renderChunk.setVBOPart(pass, null);
							} else {
								this.renderChunk.setVBOPart(pass, this.chunkRenderer.buffer(pass, bufferBuilder.getByteBuffer()));
								if (pass == ChunkRenderPass.TRANSLUCENT) {
									this.renderChunk.setTranslucentVertexData(BufferUtil.copy(bufferBuilder.getByteBuffer()));
								}
							}
						}
					}
				} finally {
					freeBuffer(bufferBuilderPack);
				}
			});

			return RenderChunkTaskResult.SUCCESSFUL;
		} catch (Throwable e) {
			freeBuffer(bufferBuilderPack);
			throw e;
		}
	}

	private void renderBlockState(IBlockState blockState, MutableBlockPos pos, VisibilityGraph visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack, Minecraft mc) {
		if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
			return;
		}

		for (Direction dir : Direction.ALL) {
			if (blockState.doesSideBlockRendering(chunkCache, pos, EnumFacingUtil.getFacing(dir))) {
				visibilityGraph.setOpaque(pos.getX(), pos.getY(), pos.getZ(), dir);
			}
		}

		// I will just quote another mod here "This is a ridiculously hacky workaround, I would not recommend it to anyone."
		blockState.getBlock().hasTileEntity(blockState);

		for (BlockRenderLayer layer : BlockRenderLayerUtil.ALL) {
			if (Nothirium.isBetterFoliageInstalled ? !BetterFoliage.canRenderBlockInLayer(blockState.getBlock(), blockState, layer) : !blockState.getBlock().canRenderInLayer(blockState, layer)) {
				continue;
			}
			ForgeHooksClient.setRenderLayer(layer);
			BufferBuilder bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(layer);
			if (!bufferBuilder.isDrawing) {
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				bufferBuilder.setTranslation(-this.renderChunk.getX(), -this.renderChunk.getY(), -this.renderChunk.getZ());
			}
			if (Nothirium.isBetterFoliageInstalled) {
				BetterFoliage.renderWorldBlock(mc.getBlockRendererDispatcher(), blockState, pos, this.chunkCache, bufferBuilder, layer);
			} else {
				mc.getBlockRendererDispatcher().renderBlock(blockState, pos, this.chunkCache, bufferBuilder);
			}
			ForgeHooksClient.setRenderLayer(null);
		}
	}

}
