package insane96mcp.stamina.module;

import insane96mcp.stamina.feature.StaminaFeature;
import insane96mcp.stamina.setup.SEnchantments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

/**
 * Thin, always-safe bridge to the optional Rune Enchanting integration ({@link SRunes}). This class never
 * references any Rune Enchanting class directly, so it loads fine even when the mod isn't installed -
 * {@link #getRuneLevel} only touches {@link SRunes} once {@link #isLoaded()} confirms it's safe to, keeping
 * {@link SRunes} (and its hard references to Rune Enchanting's classes) from ever being loaded otherwise.
 */
public class RuneCompat {
    private static final String MOD_ID = "runeenchanting";
    @Nullable
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null)
            loaded = ModList.get().isLoaded(MOD_ID);
        return loaded;
    }

    /**
     * Returns the extra enchantment level granted by a Stamina rune equivalent to {@code enchantment} on
     * {@code stack}, or 0 if there's none. Meant to be added to the item's real enchantment level.
     */
    public static int getRuneLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        if (!StaminaFeature.enchantments$runesEnabled || !isLoaded())
            return 0;

        int levelEquivalent = levelEquivalentFor(enchantment);
        if (levelEquivalent <= 0)
            return 0;

        return SRunes.getRuneLevel(stack, enchantment, levelEquivalent);
    }

    private static int levelEquivalentFor(ResourceKey<Enchantment> enchantment) {
        if (enchantment.equals(SEnchantments.VIGOUR))
            return StaminaFeature.enchantments$vigourRuneLevelEquivalent;
        // Curse of Weariness is a curse: like all curses, it only ever has a single level, so its rune
        // equivalent isn't configurable.
        if (enchantment.equals(SEnchantments.CURSE_OF_WEARINESS))
            return 1;
        return 0;
    }
}
