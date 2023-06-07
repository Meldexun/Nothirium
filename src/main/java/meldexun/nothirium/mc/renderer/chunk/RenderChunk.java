package meldexun.nothirium.mc.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.ChunkAnimator;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
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
		Chunk chunk = mc.world.getChunk(this.getSectionX(), this.getSectionZ());
		if (chunk.isEmpty()) {
			return null;
		}
		ExtendedBlockStorage blockStorage = chunk.getBlockStorageArray()[this.getSectionY()];
		if (blockStorage == null || blockStorage.isEmpty()) {
			return null;
		}
		return new RenderChunkTaskCompile(chunkRenderer, taskDispatcher, this, new SectionRenderCache(mc.world, this.getPos()));
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
	protected boolean canCompile() {
		for (int x = this.getSectionX() - 1; x <= this.getSectionX() + 1; x++) {
			for (int z = this.getSectionZ() - 1; z <= this.getSectionZ() + 1; z++) {
				if (!isChunkLoaded(x, z))
					return false;
			}
		}
		return true;
	}

	private boolean isChunkLoaded(int chunkX, int chunkZ) {
		World world = Minecraft.getMinecraft().world;
		return world != null && world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) != null;
	}

}
