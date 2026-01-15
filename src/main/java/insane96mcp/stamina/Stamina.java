package insane96mcp.stamina;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.base.Module;
import insane96mcp.stamina.command.SCommand;
import insane96mcp.stamina.data.mpr.SConditionsRegistry;
import insane96mcp.stamina.data.mpr.SPropertiesRegistry;
import insane96mcp.stamina.network.NetworkHandler;
import insane96mcp.stamina.setup.SCommonConfig;
import insane96mcp.stamina.setup.SRegistries;
import insane96mcp.stamina.stamina.StaminaFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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

    public Stamina(FMLJavaModLoadingContext context)
    {
        context.registerConfig(ModConfig.Type.COMMON, SCommonConfig.CONFIG_SPEC, MOD_ID + ".toml");

        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.register(StaminaFeature.class);

        SRegistries.REGISTRIES.forEach(register -> register.register(modEventBus));

        if (ModList.get().isLoaded("mobspropertiesrandomness")) {
            SConditionsRegistry.init();
            SPropertiesRegistry.init();
        }
    }

    public static void initModule() {
        base = Module.Builder.create(Stamina.RESOURCE_PREFIX + "base", "base", ModConfig.Type.COMMON, SCommonConfig.builder).canBeDisabled(false).build();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
