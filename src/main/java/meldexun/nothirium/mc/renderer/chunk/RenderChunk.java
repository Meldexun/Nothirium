package meldexun.nothirium.mc.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.ChunkAnimator;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import meldexun.nothirium.util.Axis;
import meldexun.nothirium.util.SectionPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
		if (this.getY() < 0 || this.getY() >= 256) {
			this.getVisibility().setAllVisible();
			return;
		}
		super.markDirty();
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
		return new RenderChunkTaskCompile(chunkRenderer, taskDispatcher, this, new SectionRenderCache(mc.world, SectionPos.of(this.getX() >> 4, this.getY() >> 4, this.getZ() >> 4)));
	}

	@Override
	@Nullable
	protected RenderChunkTaskSortTranslucent createSortTranslucentTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		IVBOPart vboPart = this.getVBOPart(ChunkRenderPass.TRANSLUCENT);
		if (vboPart == null) {
			return null;
		}
		return new RenderChunkTaskSortTranslucent(chunkRenderer, taskDispatcher, this, vboPart, this.getTranslucentVertexData());
	}

	@Override
	protected boolean allNeighborsLoaded() {
		return neighborsMatch(this, this.getX() >> 4, this.getY() >> 4, this.getZ() >> 4, Axis.X, Axis.Z, this::isNeighborLoaded);
	}

	@Override
	protected boolean isNeighborLoaded(@Nullable RenderChunk neighbor, int chunkX, int chunkY, int chunkZ) {
		if (neighbor != null) {
			return neighbor.isLoaded();
		}
		World world = Minecraft.getMinecraft().world;
		return world != null && world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) != null;
	}

}
