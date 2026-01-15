package insane96mcp.stamina.data.mpr;

import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;
import insane96mcp.stamina.Stamina;

import static insane96mcp.mobspropertiesrandomness.data.json.property.PropertiesRegistry.PROPERTIES;

public class SPropertiesRegistry {
    /// Use your own namespace
    private static void register(String id, Class<? extends MPRProperty> clazz) {
        PROPERTIES.put(Stamina.location(id), clazz);
    }

    public static void init() {
        register("set", SSetProperty.class);
        register("add", SAddProperty.class);
    }
}
