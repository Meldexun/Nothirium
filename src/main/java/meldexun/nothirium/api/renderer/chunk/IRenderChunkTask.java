package meldexun.nothirium.api.renderer.chunk;

import java.util.function.Supplier;

public interface IRenderChunkTask extends Supplier<RenderChunkTaskResult> {

	@Override
	default RenderChunkTaskResult get() {
		return this.run();
	}

	RenderChunkTaskResult run();

	boolean canceled();

	void cancel();

}
