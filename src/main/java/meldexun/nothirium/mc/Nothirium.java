package meldexun.nothirium.mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;

import meldexun.nothirium.opengl.GLHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

@Mod(modid = Nothirium.MODID)
public class Nothirium {

	public static final String MODID = "nothirium";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	private static boolean isOpenGL45Supported;

	public static boolean isGL45Supported() {
		return isOpenGL45Supported;
	}

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		GLHelper.init(GLContext.getCapabilities());
		isOpenGL45Supported = GLContext.getCapabilities().OpenGL45;
	}

}
