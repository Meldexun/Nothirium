package meldexun.nothirium.mc.renderer.chunk;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import meldexun.matrixutil.Matrix4f;
import meldexun.nothirium.api.renderer.IVBOPart;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.config.NothiriumConfig.RenderEngine;
import meldexun.nothirium.mc.util.FogUtil;
import meldexun.nothirium.mc.util.ResourceSupplier;
import meldexun.nothirium.util.ListUtil;
import meldexun.renderlib.util.GLShader;
import meldexun.renderlib.util.GLUtil;
import meldexun.renderlib.util.RenderUtil;
import net.minecraft.util.ResourceLocation;

public class ChunkRendererGL20 extends ChunkRendererDynamicVbo {

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

	@Override
	public RenderEngine getRenderEngine() {
		return RenderEngine.GL20;
	}

	@Override
	public String name() {
		return "Nothirium GL 2.0";
	}

	@Override
	public void init(int renderDistance) {
		// nothing to do
	}

	@Override
	protected void renderChunks(ChunkRenderPass pass) {
		GLShader.push();
		shader.use();
		Matrix4f matrix = RenderUtil.getProjectionModelViewMatrix();
		GLUtil.setMatrix(shader.getUniform(U_MATRIX), matrix);
		FogUtil.setupFogFromGL(shader);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos.get(pass).getVbo());
		setupClientState(pass);
		setupAttributePointers(pass);

		double cameraX = RenderUtil.getCameraEntityX();
		double cameraY = RenderUtil.getCameraEntityY();
		double cameraZ = RenderUtil.getCameraEntityZ();
		ListUtil.forEach(chunks.get(pass), pass == ChunkRenderPass.TRANSLUCENT, (renderChunk, i) -> {
			this.draw(renderChunk, pass, cameraX, cameraY, cameraZ);
		});

		resetClientState(pass);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GLShader.pop();
	}

	protected void setupClientState(ChunkRenderPass pass) {
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_POS));
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_COLOR));
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_TEXCOORD));
		GL20.glEnableVertexAttribArray(shader.getAttribute(A_LIGHTCOORD));
	}

	protected void setupAttributePointers(ChunkRenderPass pass) {
		GL20.glVertexAttribPointer(shader.getAttribute(A_POS), 3, GL11.GL_FLOAT, false, 28, 0);
		GL20.glVertexAttribPointer(shader.getAttribute(A_COLOR), 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12);
		GL20.glVertexAttribPointer(shader.getAttribute(A_TEXCOORD), 2, GL11.GL_FLOAT, false, 28, 16);
		GL20.glVertexAttribPointer(shader.getAttribute(A_LIGHTCOORD), 2, GL11.GL_SHORT, false, 28, 24);
	}

	protected void draw(RenderChunk renderChunk, ChunkRenderPass pass, double cameraX, double cameraY, double cameraZ) {
		GL20.glVertexAttrib3f(shader.getAttribute(A_OFFSET), (float) (renderChunk.getX() - cameraX), (float) (renderChunk.getY() - cameraY), (float) (renderChunk.getZ() - cameraZ));
		IVBOPart vboPart = renderChunk.getVBOPart(pass);
		GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getFirst(), vboPart.getCount());
	}

	protected void resetClientState(ChunkRenderPass pass) {
		GL20.glDisableVertexAttribArray(shader.getAttribute(A_POS));
		GL20.glDisableVertexAttribArray(shader.getAttribute(A_COLOR));
		GL20.glDisableVertexAttribArray(shader.getAttribute(A_TEXCOORD));
		GL20.glDisableVertexAttribArray(shader.getAttribute(A_LIGHTCOORD));
	}

	@Override
	public void dispose() {
		super.dispose();
		shader.dispose();
	}

}
