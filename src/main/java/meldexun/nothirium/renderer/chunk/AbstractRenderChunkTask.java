package meldexun.nothirium.renderer.chunk;

import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkTask;

public abstract class AbstractRenderChunkTask<T extends AbstractRenderChunk> implements IRenderChunkTask {

	protected final IChunkRenderer<?> chunkRenderer;
	protected final IRenderChunkDispatcher taskDispatcher;
	protected final T renderChunk;
	private volatile boolean canceled;

	protected AbstractRenderChunkTask(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher, T renderChunk) {
		this.chunkRenderer = chunkRenderer;
		this.taskDispatcher = taskDispatcher;
		this.renderChunk = renderChunk;
	}

	@Override
	public boolean canceled() {
		return this.canceled;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

}
