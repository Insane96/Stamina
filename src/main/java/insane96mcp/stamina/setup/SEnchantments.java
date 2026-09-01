package insane96mcp.stamina.setup;

import insane96mcp.stamina.Stamina;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantments are data-driven (data/stamina/enchantment/*.json), this is just a typed key pointing to
 * that data. Custom gameplay behaviour that vanilla's data-driven effects can't express (bonus max stamina
 * per level) is applied from {@link insane96mcp.stamina.feature.StaminaHandler#getMaxPossibleStamina}.
 */
public class SEnchantments {
    public static final ResourceKey<Enchantment> VIGOUR = key("vigour");

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Stamina.location(id));
    }
}
