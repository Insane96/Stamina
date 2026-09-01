package insane96mcp.stamina.feature;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.effect.VigourEffect;
import insane96mcp.stamina.event.SEventFactory;
import insane96mcp.stamina.network.StaminaSync;
import insane96mcp.stamina.setup.SRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;
import java.util.WeakHashMap;

@LoadFeature(module = Stamina.RESOURCE_PREFIX + "main", canBeDisabled = false, description = "Stamina to let the player run and do stuff.")
public class StaminaFeature extends Feature {
    public static final DeferredHolder<MobEffect, VigourEffect> VIGOUR_EFFECT = SRegistries.MOB_EFFECTS.register("vigour", () -> new VigourEffect(MobEffectCategory.BENEFICIAL, 0xFCD373));
    public static final ResourceLocation HEART_OVERLAY = Stamina.location("textures/gui/heart_overlay.png");
    public static final ResourceLocation LOCKED_HEART_OVERLAY = Stamina.location("textures/gui/locked_heart_overlay.png");

    public static final ResourceLocation LOCK_SLOWDOWN_ID = Stamina.location("lock_slowdown");
    public static final ResourceLocation SPRINT_SLOWDOWN_ID = Stamina.location("sprint_slowdown");

    public static final ResourceLocation STAMINA = Stamina.location("stamina");
    public static final ResourceLocation STAMINA_LOCKED = Stamina.location("stamina_locked");
    public static final ResourceLocation LAST_HIT = Stamina.location("last_hit");
    public static final ResourceLocation LAST_HURT = Stamina.location("last_hurt");
    public static String OVERLAY = "stamina_overlay";

    public static final DeferredHolder<Attribute, Attribute> BONUS_STAMINA_ATTRIBUTE = SRegistries.ATTRIBUTES.register("bonus_stamina", () -> (new RangedAttribute("attribute.name.bonus_stamina", 0, -Double.MAX_VALUE, Double.MAX_VALUE)).setSyncable(true));

    @Config(min = 0, description = "How much stamina the player has per half heart. Each 1 stamina is 1 tick of running")
    public static Integer stamina$perHalfHeart = 10;
    @Config(min = 0)
    public static Integer stamina$bonusPerLevelOfVigourEnchantment = 40;
    @Config(min = 0)
    public static Integer stamina$bonusPerLevelOfVigourEffect = 40;
    @Config(min = 0)
    public static Double stamina$percentageReductionPerArmorPoint = 0.025d;
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
    @Config(min = 0, description = "How much stamina the player consumes each tick when mining. If stamina is locked, mining speed is halved")
    public static Double consumption$mine = 0d;
    @Config(min = 0d, description = "Multiply stamina consumption by this value when the player is out of combat. Out of combat = not attacked or hurt in the last 15 seconds")
    public static Double consumption$outOfCombatMultiplier = 0.8d;

    @Config(min = 0d)
    public static Double regen$perTick = 2d;
    @Config(min = 0d, description = "Multiplier for the regen per tick when stamina is locked")
    public static Double regen$modifierWhenLocked = 0.6d;
    @Config(min = 0, description = "Multiplier for the regen per tick when player's in water")
    public static Double regen$modifierWhenInWater = 1d;
    @Config(min = 0, description = "'Modified when in water' is applied only when the player is not on the ground")
    public static Boolean regen$modifierWhenInWaterWhenOffGround = true;
    @Config(min = 0, description = "Percentage reduction per armor point")
    public static Double regen$reductionPerArmorPoint = 0.025d;

    @Config(min = 0, description = "When max health is equal or less than this, stamina will be locked. With locked stamina, the player can't sprint")
    public static Double lock$belowMaxHealth = 4d;
    @Config(min = 0, max = 1d, description = "When max stamina goes below this percentage, stamina will be locked. With locked stamina, the player can't sprint")
    public static Double lock$belowHealthRatio = 0.20d;
    @Config(min = 0, max = 1d, description = "At which health percentage will stamina be unlocked")
    public static Double lock$unlockAtHealthRatio = 0.4d;
    @Config(min = 0, description = "If this > 0, the player will still be able to sprint when stamina is locked, at the cost of a great amount of hunger. Stamina consumed will be applied to exhaustion at this rate. By default consumed 1 hunger/saturation per second of sprinting")
    public static Double lock$consumeHungerRatio = 0d;

    @Config(min = 0, max = 1, description = "Below this percentage stamina, sprinting will be less effective.")
    public static Double slowdown$sprinting$threshold = 0.20d;
    @Config(description = "Below this stamina amount, sprinting will be less effective.")
    public static Double slowdown$sprinting$thresholdFlat = 40d;
    @Config(min = 0, max = 1, description = "Slowdown sprinting and swimming by this amount, vanilla sprint is x1.3")
    public static Double slowdown$sprinting$amount = 0.15;
    @Config(description = "If stamina is locked, player will be slowed down.")
    public static Boolean slowdown$whenLocked$enabled = true;
    @Config(min = 0, max = 1)
    public static Double slowdown$whenLocked$amount = 0.1;

    @Config(description = "Disable sprinting altogether")
    public static Boolean disable$sprinting = false;
    @Config(description = "Disable swimming altogether")
    public static Boolean disable$swimming = false;

    @SubscribeEvent
    public static void addAttribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, BONUS_STAMINA_ATTRIBUTE))
                continue;
            event.add(entityType, BONUS_STAMINA_ATTRIBUTE);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || (disable$sprinting && disable$swimming))
            return;

        boolean shouldSync = false;

        float maxHealth = player.getMaxHealth();
        float maxPossibleStamina = StaminaHandler.getMaxPossibleStamina(player);
        float maxStamina = StaminaHandler.getMaxStamina(player, maxPossibleStamina);
        float stamina = StaminaHandler.getStamina(player);
        float maxStaminaPercentage = maxStamina / maxPossibleStamina;
        float staminaPercentage = stamina / maxStamina;
        boolean isStaminaLocked = StaminaHandler.isStaminaLocked(player);

        //Trigger sync for newly spawned players
        if (player.tickCount == 1)
            shouldSync = true;
        //Consume
        if (player.isSprinting() && player.getVehicle() == null && !player.isCreative() && !player.isSpectator()) {
            float staminaToConsume = consumption$sprint.floatValue();
            if (player.getPose() == Pose.SWIMMING)
                staminaToConsume = consumption$swim.floatValue();
            float percIncrease = 0f;
            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (instance.getEffect().value() instanceof IStaminaModifier staminaModifier)
                    percIncrease += staminaModifier.consumedStaminaModifier(instance.getAmplifier());
            }
            staminaToConsume += (staminaToConsume * percIncrease);
            if (player.getPose() == Pose.SWIMMING && player.hasEffect(MobEffects.CONDUIT_POWER))
                staminaToConsume *= consumption$conduitSwimmingModifier.floatValue();
            staminaToConsume = SEventFactory.onStaminaConsumed(player, staminaToConsume);
            if (!isInCombat(player))
                staminaToConsume *= consumption$outOfCombatMultiplier.floatValue();
            if (staminaToConsume == 0)
                return;
            StaminaHandler.consumeStamina(player, staminaToConsume);
            shouldSync = true;
        }
        //Regen
        else if (!isMining(player) && stamina != maxStamina && maxStaminaPercentage > lock$belowHealthRatio && maxHealth > lock$belowMaxHealth) {
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
            staminaToRecover *= (float) (1f - (armor * StaminaFeature.regen$reductionPerArmorPoint));

            staminaToRecover = SEventFactory.onStaminaRegenerated(player, staminaToRecover);
            if (staminaToRecover == 0)
                return;
            stamina = StaminaHandler.regenStamina(player, staminaToRecover);
            if (isStaminaLocked && staminaPercentage >= lock$unlockAtHealthRatio) {
                StaminaHandler.unlockSprinting(player);
                isStaminaLocked = false;
            }
            shouldSync = true;
        }
        else if (!isStaminaLocked && (maxStaminaPercentage <= lock$belowHealthRatio || maxHealth <= lock$belowMaxHealth)) {
            StaminaHandler.setStamina(player, 0);
            StaminaHandler.lockSprinting(player);
            isStaminaLocked = true;
            shouldSync = true;
        }
        slowdown(player, maxStaminaPercentage, staminaPercentage, stamina, isStaminaLocked);

        if (isMining(player) || shouldSync)
            StaminaSync.sync(player);
    }

    public static void slowdown(Player player, float maxStaminaPercentage, float staminaPercentage, float stamina, boolean isLocked) {
        slowdownLocked(player, isLocked);
        slowdownSprinting(player, staminaPercentage, stamina);
    }

    private static void slowdownLocked(Player player, boolean isLocked) {
        MCUtils.removeModifier(player, Attributes.MOVEMENT_SPEED, LOCK_SLOWDOWN_ID);
        MCUtils.removeModifier(player, NeoForgeMod.SWIM_SPEED, LOCK_SLOWDOWN_ID);
        if (!isLocked
                || !slowdown$whenLocked$enabled)
            return;
        MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, LOCK_SLOWDOWN_ID, -slowdown$whenLocked$amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
        MCUtils.applyModifier(player, NeoForgeMod.SWIM_SPEED, LOCK_SLOWDOWN_ID, -slowdown$whenLocked$amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
    }

    private static void slowdownSprinting(Player player, float staminaPercentage, float stamina) {
        MCUtils.removeModifier(player, Attributes.MOVEMENT_SPEED, SPRINT_SLOWDOWN_ID);
        MCUtils.removeModifier(player, NeoForgeMod.SWIM_SPEED, SPRINT_SLOWDOWN_ID);
        if (!player.isSprinting()
                || stamina >= slowdown$sprinting$thresholdFlat && staminaPercentage >= slowdown$sprinting$threshold)
            return;
        MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, SPRINT_SLOWDOWN_ID, -slowdown$sprinting$amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
        MCUtils.applyModifier(player, NeoForgeMod.SWIM_SPEED, SPRINT_SLOWDOWN_ID, -slowdown$sprinting$amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
    }

    private static final Map<ServerPlayer, Integer> tickMined = new WeakHashMap<>();

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (consumption$mine == 0)
            return;
        Player player = event.getEntity();

        if (StaminaHandler.isStaminaLocked(player)) {
            event.setNewSpeed(event.getNewSpeed() * 0.5f);
        }
        else if (player instanceof ServerPlayer serverPlayer && StaminaHandler.getStamina(player) > 0) {
            StaminaHandler.consumeStamina(player, consumption$mine.floatValue());
            tickMined.put(serverPlayer, player.tickCount);
        }
    }

    public static boolean isMining(ServerPlayer player) {
        return Feature.isEnabled(StaminaFeature.class) && tickMined.containsKey(player) && player.tickCount < tickMined.get(player) + 8;
    }

    public static boolean isInCombat(ServerPlayer player) {
        return player.level().getGameTime() - ModNBTData.get(player, LAST_HIT, Integer.class) < 300
                || player.level().getGameTime() - ModNBTData.get(player, LAST_HURT, Integer.class) < 300;
    }

    @SubscribeEvent
    public void onAttack(LivingDamageEvent.Post event) {
        if (consumption$outOfCombatMultiplier == 1f)
            return;

        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            ModNBTData.put(serverPlayer, LAST_HURT, serverPlayer.level().getGameTime());
        else if (event.getSource().getEntity() instanceof ServerPlayer serverPlayer)
            ModNBTData.put(serverPlayer, LAST_HIT, serverPlayer.level().getGameTime());
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player))
            return;

        tickMined.remove(player);
    }

    @SubscribeEvent
    public void onPlayerJump(final LivingEvent.LivingJumpEvent event) {
        if (!this.isEnabled()
                || consumption$jump == 0
                || !(event.getEntity() instanceof ServerPlayer player))
            return;

        float consumed = consumption$jump;
        float percIncrease = 0f;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value() instanceof IStaminaModifier staminaModifier)
                percIncrease += staminaModifier.consumedStaminaModifier(instance.getAmplifier());
        }
        consumed += (consumed * percIncrease);
        StaminaHandler.consumeStamina(player, consumed);
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
