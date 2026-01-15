package insane96mcp.stamina.data.mpr;

import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.stamina.Stamina;

import static insane96mcp.mobspropertiesrandomness.data.json.condition.ConditionsRegistry.CONDITIONS;

public class SConditionsRegistry {
    /// Use your own namespace
    private static void register(String id, Class<? extends MPRCondition> clazz) {
        CONDITIONS.put(Stamina.location(id), clazz);
    }

    public static void init() {
        register("stamina", SStaminaCondition.class);
    }
}
