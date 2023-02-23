package meldexun.nothirium.mc.renderer.chunk;

import java.nio.ByteBuffer;
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
import meldexun.renderlib.util.BufferUtil;
import meldexun.renderlib.util.MemoryAccess;
import meldexun.renderlib.util.UnsafeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class RenderChunkTaskSortTranslucent extends AbstractRenderChunkTask<RenderChunk> {

	private final IVBOPart vboPart;
	private final UnsafeBuffer<ByteBuffer> vertexData;

	public RenderChunkTaskSortTranslucent(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher,
			RenderChunk renderChunk, IVBOPart vboPart, ByteBuffer vertexData) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.vboPart = vboPart;
		this.vertexData = new UnsafeBuffer<>(vertexData);
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

		sortVertexData(renderChunk, vboPart.getQuadCount(), vertexData, entity.getPositionEyes(1.0F));

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

	public static void sortVertexData(RenderChunk renderChunk, int quadCount, MemoryAccess vertexData, Vec3d camera) {
		int[] quadOrder = createQuadOrder(renderChunk, quadCount, vertexData, camera);

		int bytesPerQuad = DefaultVertexFormats.BLOCK.getSize() * 4;
		UnsafeBuffer<ByteBuffer> tempBuffer = new UnsafeBuffer<>(BufferUtil.allocate(bytesPerQuad));
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
	}

	@SuppressWarnings("serial")
	private static int[] createQuadOrder(RenderChunk renderChunk, int quadCount, MemoryAccess vertexData,
			Vec3d camera) {
		float[] quadSqrDistToCam = createQuadSqrDistToCamMapping(renderChunk, quadCount, vertexData, camera);

		int[] quadOrder = IntStream.range(0, quadCount).toArray();
		IntArrays.mergeSort(quadOrder, new AbstractIntComparator() {
			@Override
			public int compare(int k1, int k2) {
				return Float.compare(quadSqrDistToCam[k2], quadSqrDistToCam[k1]);
			}
		});

		return quadOrder;
	}

	private static float[] createQuadSqrDistToCamMapping(RenderChunk renderChunk, int quadCount,
			MemoryAccess vertexData, Vec3d camera) {
		float dx = (float) (renderChunk.getX() - camera.x);
		float dy = (float) (renderChunk.getY() - camera.y);
		float dz = (float) (renderChunk.getZ() - camera.z);
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
