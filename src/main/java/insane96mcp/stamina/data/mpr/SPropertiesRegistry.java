package insane96mcp.stamina.data.mpr;

import insane96mcp.mobspropertiesrandomness.data.json.property.PropertiesRegistry;
import insane96mcp.stamina.Stamina;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class SPropertiesRegistry {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((RegisterEvent event) -> event.register(PropertiesRegistry.REGISTRY_KEY, helper -> {
            helper.register(Stamina.location("set"), SSetProperty.class);
            helper.register(Stamina.location("add"), SAddProperty.class);
        }));
    }
}
