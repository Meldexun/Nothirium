package meldexun.nothirium.mc.asm;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("meldexun.nothirium.mc.asm")
public class NothiriumPlugin implements IFMLLoadingPlugin {

	@SuppressWarnings("unchecked")
	public NothiriumPlugin() {
		try {
			if (((List<ITweaker>) Launch.blackboard.get("Tweaks")).stream().noneMatch(FMLInjectionAndSortingTweaker.class::isInstance)) {
				((List<String>) Launch.blackboard.get("TweakClasses")).add(NothiriumTweaker.class.getName());
			} else {
				((List<ITweaker>) Launch.blackboard.get("Tweaks")).add(new NothiriumTweaker());
			}
			Field _tweakSorting = CoreModManager.class.getDeclaredField("tweakSorting");
			_tweakSorting.setAccessible(true);
			((Map<String, Integer>) _tweakSorting.get(null)).put(NothiriumTweaker.class.getName(), 1001);
		} catch (ReflectiveOperationException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		if (Boolean.FALSE.equals(data.get("runtimeDeobfuscationEnabled"))) {
			MixinBootstrap.init();
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
			CoreModManager.getReparseableCoremods().removeIf(s -> StringUtils.containsIgnoreCase(s, "renderlib"));
		}
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
