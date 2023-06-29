package meldexun.nothirium.mc.renderer.chunk;

import java.util.BitSet;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL15;

import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntArrays;
import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import meldexun.renderlib.util.memory.MemoryAccess;
import meldexun.renderlib.util.memory.UnsafeBufferUtil;
import meldexun.renderlib.util.memory.UnsafeByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class RenderChunkTaskSortTranslucent extends AbstractRenderChunkTask<RenderChunk> {

	private final IVBOPart vboPart;
	private final UnsafeByteBuffer vertexData;

	public RenderChunkTaskSortTranslucent(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher,
			RenderChunk renderChunk, IVBOPart vboPart, UnsafeByteBuffer vertexData) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.vboPart = vboPart;
		this.vertexData = vertexData;
	}

	@Override
	public RenderChunkTaskResult run() {
		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}
		if (!vboPart.isValid()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (entity == null) {
			return RenderChunkTaskResult.CANCELLED;
		}

		sortVertexData(vertexData, vboPart.getQuadCount(), renderChunk, entity.getPositionEyes(1.0F));

		taskDispatcher.runOnRenderThread(() -> {
			if (!this.canceled() && vboPart.isValid()) {
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
				GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getFirst() * DefaultVertexFormats.BLOCK.getSize(),
						vertexData.getBuffer());
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
		});

		return RenderChunkTaskResult.SUCCESSFUL;
	}

	public static void sortVertexData(MemoryAccess vertexData, int quadCount, RenderChunk renderChunk, Vec3d camera) {
		sortVertexData(vertexData, quadCount, (float) (renderChunk.getX() - camera.x),
				(float) (renderChunk.getY() - camera.y), (float) (renderChunk.getZ() - camera.z));
	}

	public static void sortVertexData(MemoryAccess vertexData, int quadCount, float dx, float dy, float dz) {
		int[] quadOrder = createQuadOrder(vertexData, quadCount, dx, dy, dz);

		int bytesPerQuad = DefaultVertexFormats.BLOCK.getSize() * 4;
		UnsafeBufferUtil.tempBuffer(bytesPerQuad, tempBuffer -> {
			BitSet sortedQuads = new BitSet();
			for (int i1 = sortedQuads.nextClearBit(0); i1 < quadOrder.length; i1 = sortedQuads.nextClearBit(i1 + 1)) {
				int j1 = quadOrder[i1];

				if (j1 != i1) {
					MemoryAccess.copyMemory(vertexData, j1 * bytesPerQuad, tempBuffer, 0L, bytesPerQuad);
					int k1 = j1;

					for (int l1 = quadOrder[j1]; k1 != i1; l1 = quadOrder[l1]) {
						MemoryAccess.copyMemory(vertexData, l1 * bytesPerQuad, vertexData, k1 * bytesPerQuad, bytesPerQuad);
						sortedQuads.set(k1);
						k1 = l1;
					}

					MemoryAccess.copyMemory(tempBuffer, 0L, vertexData, i1 * bytesPerQuad, bytesPerQuad);
				}

				sortedQuads.set(i1);
			}
		});
	}

	@SuppressWarnings("serial")
	private static int[] createQuadOrder(MemoryAccess vertexData, int quadCount, float dx, float dy, float dz) {
		float[] quadSqrDistToCam = createQuadSqrDistToCamMapping(vertexData, quadCount, dx, dy, dz);

		int[] quadOrder = IntStream.range(0, quadCount).toArray();
		IntArrays.mergeSort(quadOrder, new AbstractIntComparator() {
			@Override
			public int compare(int k1, int k2) {
				return Float.compare(quadSqrDistToCam[k2], quadSqrDistToCam[k1]);
			}
		});

		return quadOrder;
	}

	private static float[] createQuadSqrDistToCamMapping(MemoryAccess vertexData, int quadCount, float dx, float dy,
			float dz) {
		int bytesPerVertex = DefaultVertexFormats.BLOCK.getSize();

		float[] quadSqrDistToCam = new float[quadCount];
		for (int i = 0; i < quadCount; i++) {
			float x0 = vertexData.getFloat((i * 4) * bytesPerVertex);
			float y0 = vertexData.getFloat((i * 4) * bytesPerVertex + 4);
			float z0 = vertexData.getFloat((i * 4) * bytesPerVertex + 8);
			float x1 = vertexData.getFloat((i * 4 + 1) * bytesPerVertex);
			float y1 = vertexData.getFloat((i * 4 + 1) * bytesPerVertex + 4);
			float z1 = vertexData.getFloat((i * 4 + 1) * bytesPerVertex + 8);
			float x2 = vertexData.getFloat((i * 4 + 2) * bytesPerVertex);
			float y2 = vertexData.getFloat((i * 4 + 2) * bytesPerVertex + 4);
			float z2 = vertexData.getFloat((i * 4 + 2) * bytesPerVertex + 8);
			float x3 = vertexData.getFloat((i * 4 + 3) * bytesPerVertex);
			float y3 = vertexData.getFloat((i * 4 + 3) * bytesPerVertex + 4);
			float z3 = vertexData.getFloat((i * 4 + 3) * bytesPerVertex + 8);
			float x = dx + (x0 + x1 + x2 + x3) * 0.25F;
			float y = dy + (y0 + y1 + y2 + y3) * 0.25F;
			float z = dz + (z0 + z1 + z2 + z3) * 0.25F;
			quadSqrDistToCam[i] = x * x + y * y + z * z;
		}

		return quadSqrDistToCam;
	}

}
