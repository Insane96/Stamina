package insane96mcp.stamina;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.base.Module;
import insane96mcp.stamina.network.NetworkHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Stamina.MOD_ID)
public class Stamina
{
    public static final String MOD_ID = "stamina";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Module base;

    public Stamina()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SCommonConfig.CONFIG_SPEC, MOD_ID + ".toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.register(StaminaFeature.class);
    }

    public static void initModule() {
        base = Module.Builder.create(Stamina.RESOURCE_PREFIX + "base", "base", ModConfig.Type.COMMON, SCommonConfig.builder).build();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }
}
