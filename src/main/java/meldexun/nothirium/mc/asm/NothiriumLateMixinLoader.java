package meldexun.nothirium.mc.asm;

import java.util.List;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

public class NothiriumLateMixinLoader implements ILateMixinLoader {

	@Override
	public List<String> getMixinConfigs() {
		return Lists.newArrayList("mixins.mods.compactmachines.json");
	}

	@Override
	public boolean shouldMixinConfigQueue(String mixinConfig) {
		switch (mixinConfig) {
			case "mixins.mods.compactmachines.json":
				return Loader.isModLoaded("compactmachines3");
		}
		return true;
	}

}
