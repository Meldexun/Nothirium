package meldexun.nothirium.mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;

import meldexun.nothirium.opengl.GLHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = Nothirium.MODID, dependencies = "required:renderlib@[1.0.7,)")
public class Nothirium {

	public static final String MODID = "nothirium";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	private static boolean isOpenGL43Supported;
	public static boolean isBetterFoliageInstalled;

	public static boolean isGL43Supported() {
		return isOpenGL43Supported;
	}

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		GLHelper.init(GLContext.getCapabilities());
		isOpenGL43Supported = GLContext.getCapabilities().OpenGL43;
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isBetterFoliageInstalled = Loader.isModLoaded("betterfoliage");
	}

}
