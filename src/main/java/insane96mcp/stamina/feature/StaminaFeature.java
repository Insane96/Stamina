package insane96mcp.stamina.feature;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.InCombat;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.effect.VigourEffect;
import insane96mcp.stamina.event.SEventFactory;
import insane96mcp.stamina.network.StaminaSync;
import insane96mcp.stamina.setup.SRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = Stamina.RESOURCE_PREFIX + "main", canBeDisabled = false, description = "Stamina to let the player run and do stuff.")
public class StaminaFeature extends Feature {
    public static final DeferredHolder<MobEffect, VigourEffect> VIGOUR_EFFECT = SRegistries.MOB_EFFECTS.register("vigour", () -> new VigourEffect(MobEffectCategory.BENEFICIAL, 0xFCD373));
    public static final ResourceLocation HEART_OVERLAY = Stamina.location("textures/gui/heart_overlay.png");
    public static final ResourceLocation LOCKED_HEART_OVERLAY = Stamina.location("textures/gui/locked_heart_overlay.png");

    public static final ResourceLocation LOCK_SLOWDOWN_ID = Stamina.location("lock_slowdown");
    public static final ResourceLocation SPRINT_SLOWDOWN_ID = Stamina.location("sprint_slowdown");

    public static final ResourceLocation STAMINA = Stamina.location("stamina");
    public static final ResourceLocation STAMINA_LOCKED = Stamina.location("stamina_locked");
    public static String OVERLAY = "stamina_overlay";

    public static final DeferredHolder<Attribute, Attribute> BONUS_STAMINA_ATTRIBUTE = SRegistries.ATTRIBUTES.register("bonus_stamina", () -> (new RangedAttribute("attribute.name.bonus_stamina", 0, -Double.MAX_VALUE, Double.MAX_VALUE)).setSyncable(true));

    @Config(min = 0, description = "How much stamina the player has per half heart. Each 1 stamina is 1 tick of running")
    public static Integer stamina$perHalfHeart = 10;
    @Config(min = 0, description = "Percentage of max stamina added per level of Vigour")
    public static Double stamina$bonusPercentagePerLevelOfVigourEnchantment = 0.20d;
    @Config(min = 0, description = "Percentage of max stamina removed per level of Curse of Weariness")
    public static Double stamina$reductionPercentagePerLevelOfCurseOfWeariness = 0.40d;
    @Config(min = 0, description = "Max stamina added per level of the Vigour effect")
    public static Integer stamina$bonusPerLevelOfVigourEffect = 40;
    @Config(min = 0, description = "Percentage of max stamina removed per armor point")
    public static Double stamina$percentageReductionPerArmorPoint = 0.025d;

    @Config(description = "If Rune Enchanting is installed, whether chestplate-only runes granting Vigour/Curse of Weariness are enabled.")
    public static Boolean enchantments$runesEnabled = true;
    @Config(min = 0, description = "Which level of Vigour the Vigour rune is equivalent to.")
    public static Integer enchantments$vigourRuneLevelEquivalent = 2;
    @Config(description = "If enabled, max stamina will always be bound to max health and no longer with current health")
    public static Boolean stamina$boundToMaxHealth = false;

    @Config(min = 0, description = "How much stamina the player consumes each tick when sprinting")
    public static Double consumption$sprint = 1d;
    @Config(min = 0, description = "How much stamina the player consumes on each jump")
    public static Integer consumption$jump = 10;
    @Config(min = 0, description = "How much stamina the player consumes each tick when swimming")
    public static Double consumption$swim = 0.5d;
    @Config(min = 0, description = "Multiplier for stamina consumed when the player is swimming with the conduit power effect.")
    public static Double consumption$conduitSwimmingModifier = 0.85d;
    @Config(min = 0d, description = "Multiply stamina consumption by this value when the player is out of combat. Out of combat = not attacked or hurt in the last 'In combat time' seconds")
    public static Double consumption$outOfCombatMultiplier = 0.8d;
    @Config(min = 0d, description = "Seconds after attacking or being hurt in which the player is considered in combat")
    public static Double consumption$inCombatTime = 15d;

    @Config(min = 0d, description = "How much stamina the player regenerates each tick when not sprinting")
    public static Double regen$perTick = 2d;
    @Config(min = 0d, description = "Multiplier for the regen per tick when stamina is locked")
    public static Double regen$modifierWhenLocked = 0.6d;
    @Config(min = 0, description = "Multiplier for the regen per tick when player's in water")
    public static Double regen$modifierWhenInWater = 1d;
    @Config(description = "'Modifier when in water' is applied only when the player is not on the ground")
    public static Boolean regen$modifierWhenInWaterWhenOffGround = true;
    @Config(min = 0, description = "Percentage of stamina regen removed per armor point")
    public static Double regen$reductionPerArmorPoint = 0.025d;

    @Config(min = 0, description = "When max health is equal or less than this, stamina will be locked and set to 0, and won't regenerate. With locked stamina, the player can't sprint")
    public static Double lock$belowMaxHealth = 4d;
    @Config(min = 0, max = 1d, description = "When health goes equal or below this percentage of max health, stamina will be locked and set to 0, and won't regenerate. With locked stamina, the player can't sprint. Has no effect if 'Bound to max health' is enabled")
    public static Double lock$belowHealthRatio = 0.20d;
    @Config(min = 0, max = 1d, description = "When stamina is locked, it gets unlocked once stamina is regenerated up to this percentage of max stamina")
    public static Double lock$unlockAtStaminaRatio = 0.4d;
    @Config(min = 0, description = "If this > 0, the player will still be able to sprint when stamina is locked, at the cost of a great amount of hunger. Stamina consumed while locked is applied as exhaustion multiplied by this value. E.g. 0.2 consumes about 1 hunger/saturation per second of sprinting")
    public static Double lock$consumeHungerRatio = 0d;

    @Config(min = 0, max = 1, description = "Below this percentage stamina, sprinting will be less effective.")
    public static Double slowdown$sprinting$threshold = 0.20d;
    @Config(description = "Below this stamina amount, sprinting will be less effective.")
    public static Double slowdown$sprinting$thresholdFlat = 40d;
    @Config(min = 0, max = 1, description = "Slowdown sprinting and swimming by this amount, vanilla sprint is x1.3")
    public static Double slowdown$sprinting$amount = 0.15;
    @Config(description = "If stamina is locked, player will be slowed down.")
    public static Boolean slowdown$whenLocked$enabled = true;
    @Config(min = 0, max = 1, description = "Slowdown walking and swimming by this amount when stamina is locked")
    public static Double slowdown$whenLocked$amount = 0.1;

    @Config(description = "Disable sprinting altogether")
    public static Boolean disable$sprinting = false;
    @Config(description = "Disable swimming altogether")
    public static Boolean disable$swimming = false;

    public static void addAttribute(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, BONUS_STAMINA_ATTRIBUTE))
            event.add(EntityType.PLAYER, BONUS_STAMINA_ATTRIBUTE);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isDeadOrDying()
                || (disable$sprinting && disable$swimming))
            return;

        boolean shouldSync = false;

        float maxHealth = player.getMaxHealth();
        float maxPossibleStamina = StaminaHandler.getMaxPossibleStamina(player);
        float maxStamina = StaminaHandler.getMaxStamina(player, maxPossibleStamina);
        float stamina = StaminaHandler.getStamina(player);
        // With no stamina at all (e.g. too much armor or Curse of Weariness) the percentages are 0, so stamina gets locked
        float maxStaminaPercentage = maxPossibleStamina > 0 ? maxStamina / maxPossibleStamina : 0f;
        float staminaPercentage = maxStamina > 0 ? stamina / maxStamina : 0f;
        boolean isStaminaLocked = StaminaHandler.isStaminaLocked(player);

        //Trigger sync for newly spawned players
        if (player.tickCount == 1)
            shouldSync = true;
        //Consume
        if (player.isSprinting() && player.getVehicle() == null && !player.isCreative() && !player.isSpectator()) {
            float staminaToConsume = consumption$sprint.floatValue();
            if (player.getPose() == Pose.SWIMMING)
                staminaToConsume = consumption$swim.floatValue();
            if (player.getPose() == Pose.SWIMMING && player.hasEffect(MobEffects.CONDUIT_POWER))
                staminaToConsume *= consumption$conduitSwimmingModifier.floatValue();
            staminaToConsume = getStaminaToConsume(player, staminaToConsume);
            if (staminaToConsume != 0) {
                StaminaHandler.consumeStamina(player, staminaToConsume);
                shouldSync = true;
            }
        }
        //Regen
        else if (stamina != maxStamina && maxStaminaPercentage > lock$belowHealthRatio && maxHealth > lock$belowMaxHealth) {
            float staminaToRecover = regen$perTick.floatValue();
            //Slower regeneration if stamina is locked
            if (isStaminaLocked)
                staminaToRecover *= regen$modifierWhenLocked.floatValue();
            if (player.isInWater() && (!player.onGround() || !regen$modifierWhenInWaterWhenOffGround) && regen$modifierWhenInWater != 1f)
                staminaToRecover *= regen$modifierWhenInWater.floatValue();
            float percIncrease = 0f;

            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (instance.getEffect().value() instanceof IStaminaModifier staminaModifier)
                    percIncrease += staminaModifier.regenStaminaModifier(instance.getAmplifier());
            }
            staminaToRecover += (staminaToRecover * percIncrease);
            double armor = player.getAttributeValue(Attributes.ARMOR);
            staminaToRecover *= (float) Math.max(0d, 1d - (armor * StaminaFeature.regen$reductionPerArmorPoint));

            staminaToRecover = SEventFactory.onStaminaRegenerated(player, staminaToRecover);
            if (staminaToRecover != 0) {
                stamina = StaminaHandler.regenStamina(player, staminaToRecover);
                shouldSync = true;
            }
        }
        else if (!isStaminaLocked && (maxStaminaPercentage <= lock$belowHealthRatio || maxHealth <= lock$belowMaxHealth)) {
            StaminaHandler.setStamina(player, 0);
            StaminaHandler.lockSprinting(player);
            isStaminaLocked = true;
            shouldSync = true;
        }
        //Unlock (checked outside regen, so stamina already full, e.g. set by a command, unlocks too)
        if (isStaminaLocked
                && maxStaminaPercentage > lock$belowHealthRatio && maxHealth > lock$belowMaxHealth
                && maxStamina > 0 && StaminaHandler.getStamina(player) / maxStamina >= lock$unlockAtStaminaRatio) {
            StaminaHandler.unlockSprinting(player);
            isStaminaLocked = false;
            shouldSync = true;
        }
        slowdown(player, staminaPercentage, stamina, isStaminaLocked);

        if (shouldSync)
            StaminaSync.sync(player);
    }

    public static void slowdown(Player player, float staminaPercentage, float stamina, boolean isLocked) {
        slowdownLocked(player, isLocked);
        slowdownSprinting(player, staminaPercentage, stamina);
    }

    private static void slowdownLocked(Player player, boolean isLocked) {
        boolean apply = isLocked && slowdown$whenLocked$enabled;
        setSlowdownModifier(player, Attributes.MOVEMENT_SPEED, LOCK_SLOWDOWN_ID, -slowdown$whenLocked$amount, apply);
        setSlowdownModifier(player, NeoForgeMod.SWIM_SPEED, LOCK_SLOWDOWN_ID, -slowdown$whenLocked$amount, apply);
    }

    private static void slowdownSprinting(Player player, float staminaPercentage, float stamina) {
        boolean apply = player.isSprinting()
                && (stamina < slowdown$sprinting$thresholdFlat || staminaPercentage < slowdown$sprinting$threshold);
        setSlowdownModifier(player, Attributes.MOVEMENT_SPEED, SPRINT_SLOWDOWN_ID, -slowdown$sprinting$amount, apply);
        setSlowdownModifier(player, NeoForgeMod.SWIM_SPEED, SPRINT_SLOWDOWN_ID, -slowdown$sprinting$amount, apply);
    }

    /**
     * Adds, updates or removes the modifier only when it actually changes, as each change marks the attribute dirty and gets it synced to the client
     */
    private static void setSlowdownModifier(Player player, Holder<Attribute> attribute, ResourceLocation id, double amount, boolean apply) {
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance == null)
            return;
        AttributeModifier modifier = attributeInstance.getModifier(id);
        if (!apply) {
            if (modifier != null)
                attributeInstance.removeModifier(id);
        }
        else if (modifier == null || modifier.amount() != amount)
            attributeInstance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @SubscribeEvent
    public void onPlayerJump(final LivingEvent.LivingJumpEvent event) {
        if (!this.isEnabled()
                || consumption$jump == 0
                || (disable$sprinting && disable$swimming)
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.isCreative()
                || player.isSpectator())
            return;

        float consumed = getStaminaToConsume(player, consumption$jump);
        if (consumed != 0)
            StaminaHandler.consumeStamina(player, consumed);
    }

    /**
     * Applies the stamina modifiers of the active effects, {@link insane96mcp.stamina.event.StaminaEvent.Consumed} and the out of combat multiplier to the stamina consumed
     */
    private static float getStaminaToConsume(ServerPlayer player, float staminaToConsume) {
        float percIncrease = 0f;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof IStaminaModifier staminaModifier)
                percIncrease += staminaModifier.consumedStaminaModifier(instance.getAmplifier());
        }
        staminaToConsume += (staminaToConsume * percIncrease);
        staminaToConsume = SEventFactory.onStaminaConsumed(player, staminaToConsume);
        if (!InCombat.isInCombat(player, consumption$inCombatTime))
            staminaToConsume *= consumption$outOfCombatMultiplier.floatValue();
        return staminaToConsume;
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        StaminaSync.sync(player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        StaminaHandler.setStamina(event.getEntity(), Float.MAX_VALUE);
    }

    public static boolean canConsumeHunger(Player player) {
        return lock$consumeHungerRatio > 0f && player.getFoodData().getFoodLevel() > 0 && !ModList.get().isLoaded("nohunger");
    }
}
