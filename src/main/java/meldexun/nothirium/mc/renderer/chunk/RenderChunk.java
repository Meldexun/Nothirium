package meldexun.nothirium.mc.renderer.chunk;

import javax.annotation.Nullable;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.mc.util.WorldUtil;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class RenderChunk extends AbstractRenderChunk {

	public RenderChunk(int x, int y, int z) {
		super(x, y, z);
	}

	@Override
	public void markDirty() {
		if (this.getSectionY() < 0 || this.getSectionY() >= 16) {
			this.getVisibility().setAllVisible();
			return;
		}
		super.markDirty();
	}

	@Override
	@Nullable
	public RenderChunkTaskCompile createCompileTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher) {
		ExtendedBlockStorage blockStorage = WorldUtil.getSection(getSectionX(), getSectionY(), getSectionZ());
		if (blockStorage == null || blockStorage.isEmpty()) {
			return null;
		}
		return new RenderChunkTaskCompile(chunkRenderer, taskDispatcher, this, new SectionRenderCache(WorldUtil.getWorld(), this.getPos()));
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
				if (!WorldUtil.isChunkLoaded(x, z))
					return false;
			}
		}
		return true;
	}

}
