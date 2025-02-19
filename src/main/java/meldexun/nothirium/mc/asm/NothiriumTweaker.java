package meldexun.nothirium.mc.asm;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class NothiriumTweaker implements ITweaker {

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.registerTransformer(NothiriumClassTransformer.class.getName());
	}

	@Override
	public String getLaunchTarget() {
		throw new RuntimeException("Invalid for use as a primary tweaker");
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}

}
