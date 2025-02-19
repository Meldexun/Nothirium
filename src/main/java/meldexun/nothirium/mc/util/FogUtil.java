package meldexun.nothirium.mc.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import meldexun.memoryutil.MemoryAccess;
import meldexun.nothirium.util.math.MathUtil;
import meldexun.renderlib.util.GLShader;
import meldexun.renderlib.util.GLUtil;

public class FogUtil {

	public static final int CYLINDRICAL_FOG = 0;
	public static final int SPHERICAL_FOG = 1;

	public static final int LINEAR_FOG = 0;
	public static final int EXP_FOG = 1;
	public static final int EXP2_FOG = 2;

	private static final String U_FOGENABLED = "u_FogEnabled";
	private static final String U_FOGSHAPE = "u_FogShape";
	private static final String U_FOGMODE = "u_FogMode";
	private static final String U_FOGSTART = "u_FogStart";
	private static final String U_FOGEND = "u_FogEnd";
	private static final String U_FOGDENSITY = "u_FogDensity";
	private static final String U_FOGCOLOR = "u_FogColor";

	private static final double FOG_THRESHOLD = -Math.log(0.001D);

	public static void setupFogFromGL(GLShader shader) {
		if (!GL11.glGetBoolean(GL11.GL_FOG)) {
			GL20.glUniform1i(shader.getUniform(U_FOGENABLED), GL11.GL_FALSE);
		} else {
			GL20.glUniform1i(shader.getUniform(U_FOGENABLED), GL11.GL_TRUE);
			GL20.glUniform1i(shader.getUniform(U_FOGSHAPE), CYLINDRICAL_FOG);
			int fogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
			if (fogMode == GL11.GL_LINEAR) {
				GL20.glUniform1i(shader.getUniform(U_FOGMODE), LINEAR_FOG);
				GL20.glUniform1f(shader.getUniform(U_FOGSTART), GL11.glGetFloat(GL11.GL_FOG_START));
				GL20.glUniform1f(shader.getUniform(U_FOGEND), GL11.glGetFloat(GL11.GL_FOG_END));
			} else if (fogMode == GL11.GL_EXP) {
				GL20.glUniform1i(shader.getUniform(U_FOGMODE), EXP_FOG);
				GL20.glUniform1f(shader.getUniform(U_FOGDENSITY), GL11.glGetFloat(GL11.GL_FOG_DENSITY));
			} else if (fogMode == GL11.GL_EXP2) {
				GL20.glUniform1i(shader.getUniform(U_FOGMODE), EXP2_FOG);
				GL20.glUniform1f(shader.getUniform(U_FOGDENSITY), GL11.glGetFloat(GL11.GL_FOG_DENSITY));
			}
			MemoryAccess fogColor = GLUtil.getFloat(GL11.GL_FOG_COLOR);
			GL20.glUniform4f(shader.getUniform(U_FOGCOLOR), fogColor.getFloat(0), fogColor.getFloat(4), fogColor.getFloat(8), fogColor.getFloat(12));
		}
	}

	public static double calculateFogEndSqr() {
		int fogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
		if (fogMode == GL11.GL_LINEAR) {
			return MathUtil.square(GL11.glGetFloat(GL11.GL_FOG_END));
		}
		if (fogMode == GL11.GL_EXP) {
			return MathUtil.square(FOG_THRESHOLD / GL11.glGetFloat(GL11.GL_FOG_DENSITY));
		}
		if (fogMode == GL11.GL_EXP2) {
			return FOG_THRESHOLD / MathUtil.square(GL11.glGetFloat(GL11.GL_FOG_DENSITY));
		}
		return Double.MAX_VALUE;
	}

}
