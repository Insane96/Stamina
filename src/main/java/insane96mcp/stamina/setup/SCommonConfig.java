package insane96mcp.stamina.setup;

import insane96mcp.insanelib.base.Module;
import insane96mcp.stamina.Stamina;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class SCommonConfig {
	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final CommonConfig COMMON;

	public static final ForgeConfigSpec.Builder builder;

	static {
		builder = new ForgeConfigSpec.Builder();
		final Pair<CommonConfig, ForgeConfigSpec> specPair = builder.configure(CommonConfig::new);
		COMMON = specPair.getLeft();
		CONFIG_SPEC = specPair.getRight();
	}

	public static class CommonConfig {
		public CommonConfig(final ForgeConfigSpec.Builder builder) {
			Stamina.initModule();
			Module.loadFeatures(ModConfig.Type.COMMON, Stamina.MOD_ID, this.getClass().getClassLoader());
		}
	}
}
