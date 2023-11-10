package meldexun.nothirium.mc.asm;

import java.util.List;

import com.google.common.collect.Lists;
import zone.rong.mixinbooter.IEarlyMixinLoader;

public class NothiriumEarlyMixinLoader implements IEarlyMixinLoader {

	@Override
	public List<String> getMixinConfigs() {
		return Lists.newArrayList("mixins.nothirium.json");
	}

}
