package meldexun.nothirium.mc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import meldexun.nothirium.opengl.GLHelper;
import meldexun.nothirium.opengl.GLTest;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = Nothirium.MODID, dependencies = "required-after:renderlib@[1.2.4,)")
public class Nothirium {

	public static final String MODID = "nothirium";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static boolean isBetterFoliageInstalled;
	public static boolean isChunkAnimatorInstalled;
	public static boolean isFluidloggedAPIInstalled;

	@EventHandler
	public void onFMLConstructionEvent(FMLConstructionEvent event) {
		GLHelper.init();
		GLTest.runTests();
	}

	@EventHandler
	public void onFMLPostInitializationEvent(FMLPostInitializationEvent event) {
		isBetterFoliageInstalled = Loader.isModLoaded("betterfoliage");
		isChunkAnimatorInstalled = Loader.isModLoaded("chunkanimator");
		isFluidloggedAPIInstalled = Loader.isModLoaded("fluidlogged_api");
	}

}
