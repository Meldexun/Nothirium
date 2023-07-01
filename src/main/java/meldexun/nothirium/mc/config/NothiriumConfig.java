package meldexun.nothirium.mc.config;

import meldexun.nothirium.mc.Nothirium;
import meldexun.renderlib.util.GLUtil;
import net.minecraftforge.common.config.Config;

@Config(modid = Nothirium.MODID)
public class NothiriumConfig {

	public enum RenderEngine {

		AUTOMATIC, GL43, GL42, GL20, GL15;

	}

	public static RenderEngine renderEngine = RenderEngine.AUTOMATIC;

	public static RenderEngine getRenderEngine() {
		return getRenderEngine(renderEngine);
	}

	private static RenderEngine getRenderEngine(RenderEngine preferredRenderEngine) {
		switch (preferredRenderEngine) {
		case AUTOMATIC:
			if (GLUtil.CAPS.OpenGL43)
				return RenderEngine.GL43;
			if (GLUtil.CAPS.OpenGL42)
				return RenderEngine.GL42;
			if (GLUtil.CAPS.OpenGL20)
				return RenderEngine.GL20;
			if (GLUtil.CAPS.OpenGL15)
				return RenderEngine.GL15;
			throw new UnsupportedOperationException();
		case GL43:
			return GLUtil.CAPS.OpenGL43 ? RenderEngine.GL43 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL42:
			return GLUtil.CAPS.OpenGL42 ? RenderEngine.GL42 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL20:
			return GLUtil.CAPS.OpenGL20 ? RenderEngine.GL20 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL15:
			return GLUtil.CAPS.OpenGL15 ? RenderEngine.GL15 : getRenderEngine(RenderEngine.AUTOMATIC);
		default:
			throw new UnsupportedOperationException();
		}
	}

}
