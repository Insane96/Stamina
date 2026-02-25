package insane96mcp.stamina.feature;

import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class StaminaHandler {
    /**
     * Returns the maximum possible stamina (when the player is fully healed)
     */
    public static float getMaxPossibleStamina(Player player) {
        float maxPossibleStamina = Mth.ceil(player.getMaxHealth()) * StaminaFeature.stamina$perHalfHeart;
        if (player.getAttribute(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get()) != null)
            maxPossibleStamina += (float) player.getAttributeValue(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get());
        if (StaminaFeature.stamina$bonusPerLevelOfVigourEnchantment > 0) {
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(StaminaFeature.VIGOUR.get(), player);
            if (enchLvl > 0)
                maxPossibleStamina += StaminaFeature.stamina$bonusPerLevelOfVigourEnchantment * enchLvl;
        }
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
                maxPossibleStamina += staminaModifier.bonusMaxStamina(instance.getAmplifier());
        }
        double armor = player.getAttributeValue(Attributes.ARMOR);
        maxPossibleStamina *= (float) (1f - (armor * StaminaFeature.stamina$percentageReductionPerArmorPoint));
        return maxPossibleStamina;
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
