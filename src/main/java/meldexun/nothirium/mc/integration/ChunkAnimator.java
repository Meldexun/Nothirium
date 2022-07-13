package meldexun.nothirium.mc.integration;

import org.lwjgl.util.vector.Vector3f;

import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class ChunkAnimator {

	private static final MutableBlockPos MUTABLE = new MutableBlockPos();
	private static final Vector3f ZERO = new Vector3f();

	public static Vector3f getOffset(IRenderChunk<?> renderChunk) {
		Vector3f offset = lumien.chunkanimator.ChunkAnimator.INSTANCE.animationHandler.getOffset(renderChunk, MUTABLE.setPos(renderChunk.getX(), renderChunk.getY(), renderChunk.getZ()));
		return offset == null ? ZERO : offset;
	}

	public static void onSetCoords(IRenderChunk<?> renderChunk) {
		lumien.chunkanimator.ChunkAnimator.INSTANCE.animationHandler.setOrigin(renderChunk, MUTABLE.setPos(renderChunk.getX(), renderChunk.getY(), renderChunk.getZ()));
	}

}
