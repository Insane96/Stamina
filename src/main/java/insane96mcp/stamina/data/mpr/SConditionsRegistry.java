package insane96mcp.stamina.data.mpr;

import insane96mcp.mobspropertiesrandomness.data.json.condition.ConditionsRegistry;
import insane96mcp.stamina.Stamina;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class SConditionsRegistry {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((RegisterEvent event) -> event.register(ConditionsRegistry.REGISTRY_KEY, helper ->
                helper.register(Stamina.location("stamina"), SStaminaCondition.class)));
    }
}
