package meldexun.nothirium.mc.renderer.chunk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.mc.Nothirium;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

public class RenderChunkDispatcher implements IRenderChunkDispatcher {

	private final ExecutorService executor = new ForkJoinPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1),
			pool -> new ForkJoinWorkerThread(pool) {
			}, (thread, exception) -> {
				Minecraft.getMinecraft().crashed(new CrashReport("Chunk Compile Thread crashed.", exception));
			}, true);
	private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
	private final Thread thread = Thread.currentThread();

	@Override
	public void update() {
		Runnable task;
		while ((task = taskQueue.poll()) != null) {
			task.run();
		}
	}

	@Override
	public <T> CompletableFuture<T> runAsync(Supplier<T> supplier) {
		return crashMinecraftOnError(CompletableFuture.supplyAsync(supplier, executor));
	}

	@Override
	public <T> CompletableFuture<T> runOnRenderThread(Supplier<T> supplier) {
		if (Thread.currentThread() == this.thread) {
			return CompletableFuture.completedFuture(supplier.get());
		} else {
			return crashMinecraftOnError(CompletableFuture.supplyAsync(supplier, this.taskQueue::add));
		}
	}

	@Override
	public CompletableFuture<Void> runAsync(Runnable runnable) {
		return crashMinecraftOnError(CompletableFuture.runAsync(runnable, executor));
	}

	@Override
	public CompletableFuture<Void> runOnRenderThread(Runnable runnable) {
		if (Thread.currentThread() == this.thread) {
			runnable.run();
			return CompletableFuture.completedFuture(null);
		} else {
			return crashMinecraftOnError(CompletableFuture.runAsync(runnable, this.taskQueue::add));
		}
	}

	private static <T> CompletableFuture<T> crashMinecraftOnError(CompletableFuture<T> future) {
		return future.whenComplete((r, t) -> {
			if (t != null) {
				Minecraft.getMinecraft().crashed(new CrashReport("Failed compiling chunk", t));
			}
		});
	}

	@Override
	public void dispose() {
		executor.shutdown();
		this.update();
		try {
			if (!executor.awaitTermination(10_000, TimeUnit.MILLISECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(10_000, TimeUnit.MILLISECONDS))
					Nothirium.LOGGER.error("ChunkRenderDispatcher did not terminate!");
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
		this.update();
	}

}
