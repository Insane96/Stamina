package insane96mcp.stamina.stamina;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class StaminaHandler {
    public static float getMaxStamina(Player player) {
        int maxStamina = Mth.ceil(player.getHealth()) * StaminaFeature.staminaPerHalfHeart;
        float ratio = player.getHealth() / player.getMaxHealth();
        if (player.getAttribute(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get()) != null)
            maxStamina += (int) (player.getAttributeValue(StaminaFeature.BONUS_STAMINA_ATTRIBUTE.get()) * ratio);
        if (StaminaFeature.staminaPerLevelOfVigourEnchantment > 0) {
            int enchLvl = EnchantmentHelper.getEnchantmentLevel(StaminaFeature.VIGOUR.get(), player);
            if (enchLvl > 0)
                maxStamina += (int) (StaminaFeature.staminaPerLevelOfVigourEnchantment * enchLvl * ratio);
        }
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
                maxStamina += (int) (staminaModifier.bonusMaxStamina(instance.getAmplifier()) * ratio);
        }
        return maxStamina;
    }

    public static float getStaminaPerHalfHeart(Player player) {
        return getMaxStamina(player) / player.getHealth();
    }

    public static float getStamina(Player player) {
        return player.getPersistentData().getFloat(StaminaFeature.STAMINA);
    }

    public static boolean isStaminaLocked(Player player) {
        return player.getPersistentData().getBoolean(StaminaFeature.STAMINA_LOCKED);
    }

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
