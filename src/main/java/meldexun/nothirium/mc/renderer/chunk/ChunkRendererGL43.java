package meldexun.nothirium.mc.renderer.chunk;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.ToIntFunction;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLSync;

import meldexun.matrixutil.Matrix4f;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.util.ResourceSupplier;
import meldexun.nothirium.util.collection.Enum2IntMap;
import meldexun.nothirium.util.collection.Enum2ObjMap;
import meldexun.nothirium.util.collection.MultiObject;
import meldexun.renderlib.util.Frustum;
import meldexun.renderlib.util.GLBuffer;
import meldexun.renderlib.util.GLShader;
import meldexun.renderlib.util.GLUtil;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

public class ChunkRendererGL43 extends ChunkRendererDynamicVbo {

	private static final String A_POS = "a_Pos";
	private static final String A_COLOR = "a_Color";
	private static final String A_TEXCOORD = "a_TexCoord";
	private static final String A_LIGHTCOORD = "a_LightCoord";
	private static final String A_OFFSET = "a_Offset";
	private static final String U_BLOCKTEX = "u_BlockTex";
	private static final String U_LIGHTTEX = "u_LightTex";
	private static final String U_MATRIX = "u_ModelViewProjectionMatrix";
	private static final String U_FOGSHAPE = "u_FogShape";
	private static final String U_FOGSTART = "u_FogStart";
	private static final String U_FOGEND = "u_FogEnd";
	private static final String U_FOGCOLOR = "u_FogColor";
	private final GLShader shader = new GLShader.Builder()
			.addShader(GL20.GL_VERTEX_SHADER, new ResourceSupplier(new ResourceLocation(Nothirium.MODID, "shaders/chunk_vert.glsl")))
			.addShader(GL20.GL_FRAGMENT_SHADER, new ResourceSupplier(new ResourceLocation(Nothirium.MODID, "shaders/chunk_frag.glsl")))
			.bindAttribute(A_POS, 0)
			.bindAttribute(A_COLOR, 1)
			.bindAttribute(A_TEXCOORD, 2)
			.bindAttribute(A_LIGHTCOORD, 3)
			.bindAttribute(A_OFFSET, 4)
			.build(p -> {
				GL20.glUniform1i(p.getUniform(U_BLOCKTEX), 0);
				GL20.glUniform1i(p.getUniform(U_LIGHTTEX), 1);
			});

	private final MultiObject<Enum2IntMap<ChunkRenderPass>> vaos;

	private final Enum2IntMap<ChunkRenderPass> chunkCounts;
	private final MultiObject<Enum2ObjMap<ChunkRenderPass, GLBuffer>> offsetBuffers;
	private final MultiObject<Enum2ObjMap<ChunkRenderPass, GLBuffer>> commandBuffers;

	private final MultiObject<GLSync> syncs;

	public ChunkRendererGL43() {
		this(2);
	}

	public ChunkRendererGL43(int bufferCount) {
		this.vaos = new MultiObject<>(bufferCount, i -> new Enum2IntMap<>(ChunkRenderPass.class));
		this.chunkCounts = new Enum2IntMap<>(ChunkRenderPass.class);
		this.offsetBuffers = new MultiObject<>(bufferCount, i -> new Enum2ObjMap<>(ChunkRenderPass.class));
		this.commandBuffers = new MultiObject<>(bufferCount, i -> new Enum2ObjMap<>(ChunkRenderPass.class));
		this.syncs = new MultiObject<>(bufferCount);
	}

	@Override
	public void init(int renderDistance) {
		int d = renderDistance * 2 + 1;
		int renderDistance3 = d * d * d;

		offsetBuffers.stream().flatMap(Enum2ObjMap::stream).filter(Objects::nonNull).forEach(GLBuffer::dispose);
		offsetBuffers.forEach(e -> e.fill(pass -> new GLBuffer(renderDistance3 * 12, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, true, GL30.GL_MAP_WRITE_BIT)));

		commandBuffers.stream().flatMap(Enum2ObjMap::stream).filter(Objects::nonNull).forEach(GLBuffer::dispose);
		commandBuffers.forEach(e -> e.fill(pass -> new GLBuffer(renderDistance3 * 16, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, true, GL30.GL_MAP_WRITE_BIT)));

		vaos.stream().flatMapToInt(Enum2IntMap::streamInt).forEach(GL30::glDeleteVertexArrays);
		vaos.forEach((i, e) -> e.fill((ToIntFunction<ChunkRenderPass>) pass -> {
			int vao = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vao);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos.get(pass).getVbo());
			GL20.glVertexAttribPointer(shader.getAttribute(A_POS), 3, GL11.GL_FLOAT, false, 28, 0);
			GL20.glVertexAttribPointer(shader.getAttribute(A_COLOR), 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12);
			GL20.glVertexAttribPointer(shader.getAttribute(A_TEXCOORD), 2, GL11.GL_FLOAT, false, 28, 16);
			GL20.glVertexAttribPointer(shader.getAttribute(A_LIGHTCOORD), 2, GL11.GL_SHORT, false, 28, 24);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, offsetBuffers.get(i).get(pass).getBuffer());
			GL20.glVertexAttribPointer(shader.getAttribute(A_OFFSET), 3, GL11.GL_FLOAT, false, 0, 0);
			GL20.glEnableVertexAttribArray(shader.getAttribute(A_POS));
			GL20.glEnableVertexAttribArray(shader.getAttribute(A_COLOR));
			GL20.glEnableVertexAttribArray(shader.getAttribute(A_TEXCOORD));
			GL20.glEnableVertexAttribArray(shader.getAttribute(A_LIGHTCOORD));
			GL20.glEnableVertexAttribArray(shader.getAttribute(A_OFFSET));
			GL33.glVertexAttribDivisor(shader.getAttribute(A_OFFSET), 1);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
			return vao;
		}));
	}

	@Override
	public void setup(IRenderChunkProvider<RenderChunk> renderChunkProvider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int frame) {
		vaos.update();
		offsetBuffers.update();
		commandBuffers.update();
		syncs.update();
		chunkCounts.fill((ToIntFunction<ChunkRenderPass>) pass -> 0);
		offsetBuffers.get().forEach(b -> b.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY));
		commandBuffers.get().forEach(b -> b.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY));

		if (syncs.get() != null) {
			GL32.glClientWaitSync(syncs.get(), 0, Long.MAX_VALUE);
			GL32.glDeleteSync(syncs.get());
			syncs.set(null);
		}

		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, frame);

		offsetBuffers.get().forEach(GLBuffer::unmap);
		commandBuffers.get().forEach(GLBuffer::unmap);
	}

	@Override
	protected void record(RenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
		if (renderChunk.isEmpty())
			return;

		for (ChunkRenderPass pass : ChunkRenderPass.ALL) {
			if (renderChunk.getVBOPart(pass) == null)
				continue;

			int baseInstance = chunkCounts.getInt(pass);
			chunkCounts.set(pass, baseInstance + 1);
			offsetBuffers.get().get(pass).getFloatBuffer()
					.put(baseInstance * 3, (float) (renderChunk.getX() - cameraX))
					.put(baseInstance * 3 + 1, (float) (renderChunk.getY() - cameraY))
					.put(baseInstance * 3 + 2, (float) (renderChunk.getZ() - cameraZ));
			if (pass != ChunkRenderPass.TRANSLUCENT) {
				commandBuffers.get().get(pass).getIntBuffer()
						.put(baseInstance * 4, renderChunk.getVBOPart(pass).getCount())
						.put(baseInstance * 4 + 1, 1)
						.put(baseInstance * 4 + 2, renderChunk.getVBOPart(pass).getFirst())
						.put(baseInstance * 4 + 3, baseInstance);
			} else {
				IntBuffer buffer = commandBuffers.get().get(pass).getIntBuffer();
				buffer.put(buffer.capacity() - (baseInstance + 1) * 4, renderChunk.getVBOPart(pass).getCount())
						.put(buffer.capacity() - (baseInstance + 1) * 4 + 1, 1)
						.put(buffer.capacity() - (baseInstance + 1) * 4 + 2, renderChunk.getVBOPart(pass).getFirst())
						.put(buffer.capacity() - (baseInstance + 1) * 4 + 3, baseInstance);
			}
		}
	}

	@Override
	public void render(ChunkRenderPass pass) {
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().entityRenderer.enableLightmap();

		GLShader.push();
		shader.use();
		Matrix4f matrix = RenderUtil.getProjectionModelViewMatrix().copy();
		matrix.translate((float) RenderUtil.getCameraOffsetX(), (float) RenderUtil.getCameraOffsetY(), (float) RenderUtil.getCameraOffsetZ());
		GLUtil.setMatrix(shader.getUniform(U_MATRIX), matrix);
		GL20.glUniform1i(shader.getUniform(U_FOGSHAPE), GL11.glGetInteger(GL11.GL_FOG));
		GL20.glUniform1f(shader.getUniform(U_FOGSTART), GL11.glGetFloat(GL11.GL_FOG_START));
		GL20.glUniform1f(shader.getUniform(U_FOGEND), GL11.glGetFloat(GL11.GL_FOG_END));
		FloatBuffer fogColor = GLUtil.getFloat(GL11.GL_FOG_COLOR);
		GL20.glUniform4f(shader.getUniform(U_FOGCOLOR), fogColor.get(0), fogColor.get(1), fogColor.get(2), fogColor.get(3));
		GL30.glBindVertexArray(vaos.get().getInt(pass));
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, commandBuffers.get().get(pass).getBuffer());

		long indirectBufferOffset = 0;
		if (pass == ChunkRenderPass.TRANSLUCENT) {
			indirectBufferOffset = commandBuffers.get().get(pass).getSize() - chunkCounts.getInt(pass) * 16;
		}
		GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, indirectBufferOffset, chunkCounts.getInt(pass), 0);
		if (pass == ChunkRenderPass.TRANSLUCENT) {
			if (syncs.get() != null)
				GL32.glDeleteSync(syncs.get());
			syncs.set(GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
		}

		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GLShader.pop();

		Minecraft.getMinecraft().entityRenderer.disableLightmap();
	}

	@Override
	public void dispose() {
		super.dispose();
		shader.dispose();
		offsetBuffers.stream().flatMap(Enum2ObjMap::stream).filter(Objects::nonNull).forEach(GLBuffer::dispose);
		commandBuffers.stream().flatMap(Enum2ObjMap::stream).filter(Objects::nonNull).forEach(GLBuffer::dispose);
		vaos.stream().flatMapToInt(Enum2IntMap::streamInt).forEach(GL30::glDeleteVertexArrays);
		syncs.stream().filter(Objects::nonNull).forEach(GL32::glDeleteSync);
	}

}
