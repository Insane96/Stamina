package insane96mcp.stamina.feature;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.stamina.module.RuneCompat;
import insane96mcp.stamina.setup.SEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class StaminaHandler {
    /**
     * Returns the maximum possible stamina (when the player is fully healed)
     */
    public static float getMaxPossibleStamina(Player player) {
        float maxPossibleStamina = Mth.ceil(player.getMaxHealth()) * StaminaFeature.stamina$perHalfHeart;
        if (player.getAttribute(StaminaFeature.BONUS_STAMINA_ATTRIBUTE) != null)
            maxPossibleStamina += (float) player.getAttributeValue(StaminaFeature.BONUS_STAMINA_ATTRIBUTE);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (StaminaFeature.stamina$bonusPercentagePerLevelOfVigourEnchantment > 0) {
            int enchLvl = getEnchantmentLevel(player, chest, SEnchantments.VIGOUR) + RuneCompat.getRuneLevel(chest, SEnchantments.VIGOUR);
            if (enchLvl > 0)
                maxPossibleStamina *= (float) (1d + StaminaFeature.stamina$bonusPercentagePerLevelOfVigourEnchantment * enchLvl);
        }
        if (StaminaFeature.stamina$reductionPercentagePerLevelOfCurseOfWeariness > 0) {
            int curseLvl = getEnchantmentLevel(player, chest, SEnchantments.CURSE_OF_WEARINESS) + RuneCompat.getRuneLevel(chest, SEnchantments.CURSE_OF_WEARINESS);
            if (curseLvl > 0)
                maxPossibleStamina *= (float) Math.max(0d, 1d - StaminaFeature.stamina$reductionPercentagePerLevelOfCurseOfWeariness * curseLvl);
        }
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof IStaminaModifier staminaModifier)
                maxPossibleStamina += staminaModifier.bonusMaxStamina(instance.getAmplifier());
        }
        double armor = player.getAttributeValue(Attributes.ARMOR);
        maxPossibleStamina *= (float) (1f - (armor * StaminaFeature.stamina$percentageReductionPerArmorPoint));
        return Math.max(0f, maxPossibleStamina);
    }

    /**
     * Returns the level of the enchantment on the stack, or 0 if the enchantment isn't in the registry (e.g. filtered out by a datapack)
     */
    private static int getEnchantmentLevel(Player player, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        return player.level().registryAccess().lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(enchantment))
                .map(holder -> stack.getEnchantmentLevel(holder))
                .orElse(0);
    }

    /**
     * Returns the current max stamina
     */
    public static float getMaxStamina(Player player) {
        if (StaminaFeature.stamina$boundToMaxHealth)
            return getMaxPossibleStamina(player);
        float ratio = player.getHealth() / player.getMaxHealth();
        return getMaxPossibleStamina(player) * ratio;
    }

    /**
     * Returns the current max stamina
     */
    public static float getMaxStamina(Player player, float maxPossibleStamina) {
        if (StaminaFeature.stamina$boundToMaxHealth)
            return maxPossibleStamina;
        float ratio = player.getHealth() / player.getMaxHealth();
        return maxPossibleStamina * ratio;
    }

    /**
     * Returns the current stamina
     */
    public static float getStamina(Player player) {
        return ModNBTData.get(player, StaminaFeature.STAMINA, Float.class);
    }

    /**
     * Returns true if the player is locked from sprinting
     */
    public static boolean isStaminaLocked(Player player) {
        return ModNBTData.get(player, StaminaFeature.STAMINA_LOCKED, Boolean.class);
    }

    /**
     * Returns true if the player can sprint
     */
    public static boolean canSprint(Player player) {
        return !isStaminaLocked(player) || StaminaFeature.canConsumeHunger(player);
    }

    public static float setStamina(Player player, float stamina) {
        stamina = Mth.clamp(stamina, 0, getMaxStamina(player));
        ModNBTData.put(player, StaminaFeature.STAMINA, stamina);
        return stamina;
    }

    public static void consumeStamina(Player player, float amount) {
        float staminaSetTo = setStamina(player, getStamina(player) - amount);
        if (staminaSetTo <= 0)
            lockSprinting(player);
        if (StaminaFeature.canConsumeHunger(player) && isStaminaLocked(player))
            player.getFoodData().addExhaustion(StaminaFeature.lock$consumeHungerRatio.floatValue() * amount);
    }

    public static float regenStamina(Player player, float amount) {
        return setStamina(player, getStamina(player) + amount);
    }

    public static void lockSprinting(Player player) {
        ModNBTData.put(player, StaminaFeature.STAMINA_LOCKED, true);
    }

    public static void unlockSprinting(Player player) {
        ModNBTData.put(player, StaminaFeature.STAMINA_LOCKED, false);
    }
}
