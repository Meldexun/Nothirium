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

		double tx = entity.posX - renderChunk.getX();
		double ty = (entity.posY + entity.getEyeHeight()) - renderChunk.getY();
		double tz = entity.posZ - renderChunk.getZ();
		int quadCount = vboPart.getCount() / 4;
		int vertexSize = DefaultVertexFormats.BLOCK.getSize();

		double[] quadDistToCam = IntStream.range(0, quadCount).mapToDouble(quad -> {
			double x0 = byteBuffer.getFloat((quad * 4) * vertexSize);
			double y0 = byteBuffer.getFloat((quad * 4) * vertexSize + 4);
			double z0 = byteBuffer.getFloat((quad * 4) * vertexSize + 8);
			double x1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize);
			double y1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize + 4);
			double z1 = byteBuffer.getFloat((quad * 4 + 1) * vertexSize + 8);
			double x2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize);
			double y2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize + 4);
			double z2 = byteBuffer.getFloat((quad * 4 + 2) * vertexSize + 8);
			double x3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize);
			double y3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize + 4);
			double z3 = byteBuffer.getFloat((quad * 4 + 3) * vertexSize + 8);
			double x = (x0 + x1 + x2 + x3) * 0.25D - tx;
			double y = (y0 + y1 + y2 + y3) * 0.25D - ty;
			double z = (z0 + z1 + z2 + z3) * 0.25D - tz;
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
				intBuffer.limit(index * vertexSize + vertexSize);
				intBuffer.position(index * vertexSize);
				intBuffer.get(temp);
				int j = index;

				for (int k = quadIndices[index].intValue(); j != i; k = quadIndices[k].intValue()) {
					intBuffer.limit(k * vertexSize + vertexSize);
					intBuffer.position(k * vertexSize);
					IntBuffer intbuffer = intBuffer.slice();
					intBuffer.limit(j * vertexSize + vertexSize);
					intBuffer.position(j * vertexSize);
					intBuffer.put(intbuffer);
					bitset.set(j);
					j = k;
				}

				intBuffer.limit(i * vertexSize + vertexSize);
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
