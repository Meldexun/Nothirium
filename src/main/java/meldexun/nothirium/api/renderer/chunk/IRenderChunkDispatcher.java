package meldexun.nothirium.api.renderer.chunk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface IRenderChunkDispatcher {

	void update();

	<T> CompletableFuture<T> runAsync(Supplier<T> supplier);

	<T> CompletableFuture<T> runOnRenderThread(Supplier<T> supplier);

	CompletableFuture<Void> runAsync(Runnable runnable);

	CompletableFuture<Void> runOnRenderThread(Runnable runnable);

	void dispose();

}
