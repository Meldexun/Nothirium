package meldexun.nothirium.mc.renderer.chunk;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL15;

import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;

public class RenderChunkTaskSortTranslucent extends AbstractRenderChunkTask<RenderChunk> {

	private final IVBOPart vboPart;
	private final ByteBuffer byteBuffer;

	public RenderChunkTaskSortTranslucent(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher, RenderChunk renderChunk, IVBOPart vboPart,
			ByteBuffer byteBuffer) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.vboPart = vboPart;
		this.byteBuffer = byteBuffer;
	}

	@Override
	public RenderChunkTaskResult run() {
		if (this.canceled())
			return RenderChunkTaskResult.CANCELLED;

		if (!vboPart.isValid())
			return RenderChunkTaskResult.CANCELLED;

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

		if (entity == null)
			return RenderChunkTaskResult.CANCELLED;

		float tx = (float) entity.posX - renderChunk.getX();
		float ty = (float) ((entity.posY + entity.getEyeHeight()) - renderChunk.getY());
		float tz = (float) entity.posZ - renderChunk.getZ();
		int quadCount = vboPart.getCount() / 4;
		int vertexSize = DefaultVertexFormats.BLOCK.getSize();

		double[] quadDistToCam = IntStream.range(0, quadCount).mapToDouble(quad -> {
			float x0 = byteBuffer.getFloat((quad * 4) * vertexSize);
			float y0 = byteBuffer.getFloat((quad * 4) * vertexSize + 4);
			float z0 = byteBuffer.getFloat((quad * 4) * vertexSize + 8);
			float x1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize);
			float y1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize + 4);
			float z1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize + 8);
			float x2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize);
			float y2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize + 4);
			float z2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize + 8);
			float x3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize);
			float y3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize + 4);
			float z3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize + 8);
			double x = (x0 + x1 + x2 + x3) * 0.25F - tx;
			double y = (y0 + y1 + y2 + y3) * 0.25F - ty;
			double z = (z0 + z1 + z2 + z3) * 0.25F - tz;
			return x * x + y * y + z * z;
		}).toArray();

		Integer[] quadIndices = IntStream.range(0, quadCount).boxed().toArray(Integer[]::new);
		Arrays.sort(quadIndices, Comparator.<Integer>comparingDouble(index -> quadDistToCam[index]).reversed());

		boolean sorted = true;
		for (int i = 0; i < quadIndices.length; i++) {
			if (quadIndices[i].intValue() != i) {
				sorted = false;
				break;
			}
		}
		if (sorted) {
			return RenderChunkTaskResult.SUCCESSFUL;
		}

		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		BitSet bitset = new BitSet();
		int[] temp = new int[vertexSize];

		for (int i = bitset.nextClearBit(0); i < quadIndices.length; i = bitset.nextClearBit(i + 1)) {
			int index = quadIndices[i].intValue();

			if (index != i) {
				intBuffer.limit((index + 1) * vertexSize);
				intBuffer.position(index * vertexSize);
				intBuffer.get(temp);
				int j = index;

				for (int k = quadIndices[index].intValue(); j != i; k = quadIndices[k].intValue()) {
					intBuffer.limit((k + 1) * vertexSize);
					intBuffer.position(k * vertexSize);
					IntBuffer intbuffer = intBuffer.slice();
					intBuffer.limit((j + 1) * vertexSize);
					intBuffer.position(j * vertexSize);
					intBuffer.put(intbuffer);
					bitset.set(j);
					j = k;
				}

				intBuffer.limit((i + 1) * vertexSize);
				intBuffer.position(i * vertexSize);
				intBuffer.put(temp);
			}

			bitset.set(i);
		}

		taskDispatcher.runOnRenderThread(() -> {
			if (!this.canceled() && vboPart.isValid()) {
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
				GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getFirst() * vertexSize, byteBuffer);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
		});

		return RenderChunkTaskResult.SUCCESSFUL;
	}

}
