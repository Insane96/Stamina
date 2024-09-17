package insane96mcp.stamina.stamina;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class StaminaHandler {
    /**
     * Returns the maximum possible stamina (when the player is fully healed)
     */
    public static float getMaxPossibleStamina(Player player) {
        float maxPossibleStamina = Mth.ceil(player.getMaxHealth()) * StaminaFeature.staminaPerHalfHeart;
        if (player.getAttribute(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get()) != null)
            maxPossibleStamina += (float) player.getAttributeValue(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get());
        if (StaminaFeature.staminaPerLevelOfVigourEnchantment > 0) {
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(StaminaFeature.VIGOUR.get(), player);
            if (enchLvl > 0)
                maxPossibleStamina += StaminaFeature.staminaPerLevelOfVigourEnchantment * enchLvl;
        }
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
                maxPossibleStamina += staminaModifier.bonusMaxStamina(instance.getAmplifier());
        }
        return maxPossibleStamina;
    }

    /**
     * Returns the current max stamina
     */
    public static float getMaxStamina(Player player) {
        float ratio = player.getHealth() / player.getMaxHealth();
        return getMaxPossibleStamina(player) * ratio;
    }

    /**
     * Returns the current max stamina
     */
    public static float getMaxStamina(Player player, float maxPossibleStamina) {
        float ratio = player.getHealth() / player.getMaxHealth();
        return maxPossibleStamina * ratio;
    }

    /*public static float getStaminaPerHalfHeart(Player player) {
        return getMaxStamina(player) / player.getHealth();
    }*/

    /**
     * Returns the current stamina
     */
    public static float getStamina(Player player) {
        return player.getPersistentData().getFloat(StaminaFeature.STAMINA);
    }

    /**
     * Returns true if the player is locked from sprinting
     */
    public static boolean isStaminaLocked(Player player) {
        return player.getPersistentData().getBoolean(StaminaFeature.STAMINA_LOCKED);
    }

    /**
     * Returns true if the player can sprint
     */
    public static boolean canSprint(Player player) {
        return !isStaminaLocked(player);
    }

    public static float setStamina(Player player, float stamina) {
        stamina = Mth.clamp(stamina, 0, getMaxStamina(player));
        player.getPersistentData().putFloat(StaminaFeature.STAMINA, stamina);
        return stamina;
    }

    public static void consumeStamina(Player player, float amount) {
        float staminaSetTo = setStamina(player, getStamina(player) - amount);
        if (staminaSetTo <= 0)
            lockSprinting(player);
    }

    public static float regenStamina(Player player, float amount) {
        return setStamina(player, getStamina(player) + amount);
    }

    public static void lockSprinting(Player player) {
        player.getPersistentData().putBoolean(StaminaFeature.STAMINA_LOCKED, true);
    }

    public static void unlockSprinting(Player player) {
        player.getPersistentData().putBoolean(StaminaFeature.STAMINA_LOCKED, false);
    }
}
