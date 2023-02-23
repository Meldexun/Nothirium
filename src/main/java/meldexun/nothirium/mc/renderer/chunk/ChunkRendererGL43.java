package meldexun.nothirium.mc.renderer.chunk;

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
import org.lwjgl.util.vector.Vector3f;

import meldexun.matrixutil.Matrix4f;
import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.integration.ChunkAnimator;
import meldexun.nothirium.mc.util.FogUtil;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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

	private final MultiObject<Enum2ObjMap<ChunkRenderPass, GLBuffer>> offsetBuffers;
	private final MultiObject<Enum2ObjMap<ChunkRenderPass, GLBuffer>> commandBuffers;

	private final MultiObject<GLSync> syncs;

	public ChunkRendererGL43() {
		this(2);
	}

	public ChunkRendererGL43(int bufferCount) {
		this.vaos = new MultiObject<>(bufferCount, i -> new Enum2IntMap<>(ChunkRenderPass.class));
		this.offsetBuffers = new MultiObject<>(bufferCount, i -> new Enum2ObjMap<>(ChunkRenderPass.class));
		this.commandBuffers = new MultiObject<>(bufferCount, i -> new Enum2ObjMap<>(ChunkRenderPass.class));
		this.syncs = new MultiObject<>(bufferCount);
	}

	@Override
	public String name() {
		return "Nothirium GL 4.3";
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
		super.setup(renderChunkProvider, cameraX, cameraY, cameraZ, frustum, frame);

		vaos.update();
		offsetBuffers.update();
		commandBuffers.update();
		syncs.update();
		offsetBuffers.get().forEach(b -> b.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY));
		commandBuffers.get().forEach(b -> b.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY));

		if (syncs.get() != null) {
			GL32.glClientWaitSync(syncs.get(), 0, Long.MAX_VALUE);
			GL32.glDeleteSync(syncs.get());
			syncs.set(null);
		}

		this.chunks.forEach((pass, list) -> {
			if (pass != ChunkRenderPass.TRANSLUCENT) {
				for (int i = 0; i < list.size(); i++) {
					this.record(list.get(i), pass, i, cameraX, cameraY, cameraZ);
				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					this.record(list.get(list.size() - 1 - i), pass, i, cameraX, cameraY, cameraZ);
				}
			}
		});

		offsetBuffers.get().forEach(GLBuffer::unmap);
		commandBuffers.get().forEach(GLBuffer::unmap);
	}

	protected void record(RenderChunk renderChunk, ChunkRenderPass pass, int index, double cameraX, double cameraY, double cameraZ) {
		if (Nothirium.isChunkAnimatorInstalled) {
			Vector3f offset = ChunkAnimator.getOffset(renderChunk);
			cameraX -= offset.x;
			cameraY -= offset.y;
			cameraZ -= offset.z;
		}

		GLBuffer offsetBuffer = offsetBuffers.get().get(pass);
		offsetBuffer.putFloat(index * 12, (float) (renderChunk.getX() - cameraX));
		offsetBuffer.putFloat(index * 12 + 4, (float) (renderChunk.getY() - cameraY));
		offsetBuffer.putFloat(index * 12 + 8, (float) (renderChunk.getZ() - cameraZ));

		IVBOPart vboPart = renderChunk.getVBOPart(pass);
		GLBuffer commandBuffer = commandBuffers.get().get(pass);
		commandBuffer.putInt(index * 16, vboPart.getCount());
		commandBuffer.putInt(index * 16 + 4, 1);
		commandBuffer.putInt(index * 16 + 8, vboPart.getFirst());
		commandBuffer.putInt(index * 16 + 12, index);
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
		FogUtil.setupFogFromGL(shader);
		GL30.glBindVertexArray(vaos.get().getInt(pass));
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, commandBuffers.get().get(pass).getBuffer());

		GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, chunks.get(pass).size(), 0);
		if (pass == ChunkRenderPass.TRANSLUCENT) {
			if (syncs.get() != null)
				GL32.glDeleteSync(syncs.get());
			syncs.set(GL32.glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0));
		}

		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GLShader.pop();

		GlStateManager.resetColor();
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

	@Override
	protected boolean isSpectator() {
		Minecraft mc = Minecraft.getMinecraft();
		Entity cameraEntity = mc.getRenderViewEntity();
		return cameraEntity instanceof EntityPlayer && ((EntityPlayer) cameraEntity).isSpectator();
	}

}
