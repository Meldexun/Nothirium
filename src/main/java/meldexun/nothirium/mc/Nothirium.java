package meldexun.nothirium.mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import meldexun.nothirium.opengl.GLHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = Nothirium.MODID, dependencies = "required-after:renderlib@[1.1.0,)")
public class Nothirium {

	public static final String MODID = "nothirium";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static boolean isBetterFoliageInstalled;
	public static boolean isChunkAnimatorInstalled;

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		GLHelper.init();
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isBetterFoliageInstalled = Loader.isModLoaded("betterfoliage");
		isChunkAnimatorInstalled = Loader.isModLoaded("chunkanimator");
	}

}
