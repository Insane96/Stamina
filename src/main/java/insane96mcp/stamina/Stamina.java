package insane96mcp.stamina;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.stamina.command.SCommand;
import insane96mcp.stamina.feature.StaminaFeature;
import insane96mcp.stamina.network.NetworkHandler;
import insane96mcp.stamina.setup.SRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(Stamina.MOD_ID)
public class Stamina
{
    public static final String MOD_ID = "stamina";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;

    public Stamina(IEventBus modEventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(location("main"), "Single Module", ModConfig.Type.SERVER, modEventBus, Stamina.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.SERVER, CONFIG.spec);

        NeoForge.EVENT_BUS.register(this);

        SRegistries.REGISTRIES.forEach(register -> register.register(modEventBus));

        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(StaminaFeature::addAttribute);

        // TODO Mobs Properties Randomness and Rune Enchanting have no 26.1.2 version yet, re-enable (with the dependencies in build.gradle) when they're ported
        //if (ModList.get().isLoaded("mobspropertiesrandomness")) {
        //    SConditionsRegistry.init(modEventBus);
        //    SPropertiesRegistry.init(modEventBus);
        //}

        //if (RuneCompat.isLoaded())
        //    new SRunes(modEventBus);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SCommand.register(event.getDispatcher());
    }

    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
