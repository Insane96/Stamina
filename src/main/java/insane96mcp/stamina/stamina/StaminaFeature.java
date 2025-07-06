package insane96mcp.stamina.stamina;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.PlayerSprintEvent;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.effect.VigourEffect;
import insane96mcp.stamina.enchantment.VigourEnchantment;
import insane96mcp.stamina.event.SEventFactory;
import insane96mcp.stamina.mixin.GuiAccessor;
import insane96mcp.stamina.network.StaminaSync;
import insane96mcp.stamina.setup.SRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = Stamina.RESOURCE_PREFIX + "base", canBeDisabled = false, description = "Stamina to let the player run and do stuff.")
public class StaminaFeature extends Feature {
    public static final RegistryObject<MobEffect> VIGOUR_EFFECT = SRegistries.MOB_EFFECTS.register("vigour", () -> new VigourEffect(MobEffectCategory.BENEFICIAL, 0xFCD373));
    public static final ResourceLocation HEART_OVERLAY = Stamina.location("textures/gui/heart_overlay.png");
    public static final ResourceLocation LOCKED_HEART_OVERLAY = Stamina.location("textures/gui/locked_heart_overlay.png");

    public static final UUID LOCK_SLOWDOWN_UUID = UUID.fromString("b17cbf02-97f8-4c50-9cd1-6dc732593fed");
    public static final UUID SPRINT_SLOWDOWN_UUID = UUID.fromString("d5c66a92-3f1f-44a2-95a6-1a9e66c6d8e5");

    public static final ResourceLocation STAMINA = Stamina.location("stamina");
    public static final ResourceLocation STAMINA_LOCKED = Stamina.location("stamina_locked");
    public static final ResourceLocation LAST_HIT = Stamina.location("last_hit");
    public static final ResourceLocation LAST_HURT = Stamina.location("last_hurt");
    public static String OVERLAY = "stamina_overlay";

    public static final RegistryObject<Enchantment> VIGOUR = SRegistries.ENCHANTMENTS.register("vigour", VigourEnchantment::new);

    public static final RegistryObject<Attribute> BONUS_STAMINA_ATTRIBUTE = SRegistries.ATTRIBUTES.register("bonus_stamina", () -> (new RangedAttribute("attribute.name.bonus_stamina", 0, -Double.MAX_VALUE, Double.MAX_VALUE)).setSyncable(true));

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
    @Config(min = 0d, description = "Stamina consumption will be multiplied by this value when the player is out of combat. Out of combat = not attacked or hurt in the last 15 seconds")
    public static Double consumption$outOfCombatMultiplier = 0.8d;

    @Config(min = 0d)
    public static Double regen$perTick = 2d;
    @Config(min = 0d, description = "Multiplier for the regen per tick when stamina is locked")
    public static Double regen$modifierWhenLocked = 0.6d;
    @Config(min = 0, description = "Percentage reduction per armor point")
    public static Double regen$reductionPerArmorPoint = 0.025d;

    @Config(min = 0, max = 1d, description = "When max stamina goes below this percentage, stamina will be locked. With locked stamina, the player can't sprint")
    public static Double lock$belowHealthRatio = 0.20d;
    @Config(min = 0, max = 1d, description = "At which health percentage will stamina be unlocked")
    public static Double lock$unlockAtHealthRatio = 0.4d;
    @Config(min = 0, description = "If this > 0, the player will still be able to sprint when stamina is locked, at the cost of a great amount of hunger. Stamina consumed will be applied to exhaustion at this rate. By default consumed 1 hunger/saturation per second of sprinting")
    public static Double lock$consumeHungerRatio = 0.1d;

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
            if (event.has(entityType, BONUS_STAMINA_ATTRIBUTE.get()))
                continue;

            event.add(entityType, BONUS_STAMINA_ATTRIBUTE.get());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)
                || event.phase.equals(TickEvent.Phase.START)
                || (disable$sprinting && disable$swimming))
            return;

        boolean shouldSync = false;

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
                if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
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
        else if (!isMining(player) && stamina != maxStamina && maxStaminaPercentage >= lock$belowHealthRatio) {
            float staminaToRecover = regen$perTick.floatValue();
            //Slower regeneration if stamina is locked
            if (isStaminaLocked)
                staminaToRecover *= regen$modifierWhenLocked.floatValue();
            float percIncrease = 0f;

            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
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
        else if (!isStaminaLocked && maxStaminaPercentage < lock$belowHealthRatio) {
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
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(LOCK_SLOWDOWN_UUID);
        if (!isLocked
                || !slowdown$whenLocked$enabled)
            return;
        MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, LOCK_SLOWDOWN_UUID, "Stamina locked slowdown", -slowdown$whenLocked$amount, AttributeModifier.Operation.MULTIPLY_TOTAL, false);
        MCUtils.applyModifier(player, ForgeMod.SWIM_SPEED.get(), LOCK_SLOWDOWN_UUID, "Stamina locked slowdown", -slowdown$whenLocked$amount, AttributeModifier.Operation.MULTIPLY_TOTAL, false);
    }

    private static void slowdownSprinting(Player player, float staminaPercentage, float stamina) {
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPRINT_SLOWDOWN_UUID);
        if (!player.isSprinting()
                || stamina >= slowdown$sprinting$thresholdFlat && staminaPercentage >= slowdown$sprinting$threshold)
            return;
        MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, LOCK_SLOWDOWN_UUID, "Stamina sprinting slowdown", -slowdown$sprinting$amount, AttributeModifier.Operation.MULTIPLY_TOTAL, false);
        MCUtils.applyModifier(player, ForgeMod.SWIM_SPEED.get(), LOCK_SLOWDOWN_UUID, "Stamina swimming slowdown", -slowdown$sprinting$amount, AttributeModifier.Operation.MULTIPLY_TOTAL, false);
    }

    private static final Map<ServerPlayer, Integer> tickMined = new HashMap<>();

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
    public void onAttack(LivingDamageEvent event) {
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onSprint(PlayerSprintEvent event) {
        if (!this.isEnabled()
                || event.getPlayer().getAbilities().instabuild)
            return;

        if (!StaminaHandler.canSprint(event.getPlayer()) || (disable$sprinting && !event.getPlayer().canStartSwimming()) || (disable$swimming && event.getPlayer().canStartSwimming()))
            event.setCanceled(true);
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
            if (instance.getEffect() instanceof IStaminaModifier staminaModifier)
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), OVERLAY, (gui, guiGraphics, partialTicks, screenWidth, screenHeight) -> {
            if (isEnabled(StaminaFeature.class) && gui.shouldDrawSurvivalElements())
                renderStamina(gui, guiGraphics);
        });
    }

    public static boolean canConsumeHunger(Player player) {
        return lock$consumeHungerRatio > 0f && player.getFoodData().getFoodLevel() > 0 && !ModList.get().isLoaded("nohunger");
    }

    private static final Vec2 UV_STAMINA = new Vec2(0, 9);

    @OnlyIn(Dist.CLIENT)
    public static void renderStamina(ForgeGui gui, GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;

        ((GuiAccessor) gui).getRandom().setSeed(gui.getGuiTicks() * 312871L);

        boolean shouldRenderOnOneRow = ModList.get().isLoaded("mantle");

        int health = Mth.ceil(player.getHealth());
        if (StaminaFeature.stamina$boundToMaxHealth)
            health = Mth.ceil(player.getMaxHealth());

        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float) attrMaxHealth.getValue(),  health);
        int healthMaxI = Mth.ceil(healthMax);
        int absorp = Mth.ceil(player.getAbsorptionAmount());
        int halfAbsorp = Mth.ceil(player.getAbsorptionAmount() / 2);

        int healthRows = Mth.ceil((healthMax + absorp) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int leftHeight = gui.leftHeight;
        if (!shouldRenderOnOneRow) {
            leftHeight -= (healthRows * rowHeight);
            if (rowHeight != 10)
                leftHeight -= 10 - rowHeight;
        }
        else
            leftHeight -= 10;

        int right = mc.getWindow().getGuiScaledWidth() / 2 - 91;
        int top = mc.getWindow().getGuiScaledHeight() - leftHeight;
        float staminaPerHalfHeart = StaminaHandler.getMaxStamina(player) / health;
        if (shouldRenderOnOneRow)
            staminaPerHalfHeart = StaminaHandler.getMaxStamina(player) / Math.min(health, 20f);
        int halfHeartsMaxStamina = Mth.ceil(StaminaHandler.getMaxStamina(player) / staminaPerHalfHeart);
        int halfHeartsStamina = Mth.ceil(StaminaHandler.getStamina(player) / staminaPerHalfHeart);
        int height = 9;
        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION))
            regen = gui.getGuiTicks() % Mth.ceil(healthMax + 5.0F);

        ResourceLocation texture = HEART_OVERLAY;
        if (StaminaHandler.isStaminaLocked(player))
            texture = LOCKED_HEART_OVERLAY;

        int oldJiggle = 0;

        for (int a = 0; a < halfAbsorp; a++) {
            ((GuiAccessor) gui).getRandom().nextInt(2);
        }

        for (int hp = healthMaxI - 1; hp >= 0; hp--) {
            //Doesn't work with absorption ...
            int jiggle = 0;
            if ((hp + 1) % 2 == 0) {
                if (hp / 2 == regen)
                    jiggle -= 2;
                if (health + absorp <= 4)
                    jiggle += ((GuiAccessor) gui).getRandom().nextInt(2);
                oldJiggle = jiggle;
            }
            else
                jiggle = oldJiggle;
            if (hp >= halfHeartsMaxStamina || hp < halfHeartsStamina)
                continue;
            int v = (int) UV_STAMINA.y;
            int width;
            int u;
            int r;
            if (hp % 2 == 0) {
                width = 4;
                u = (int) UV_STAMINA.x + 1;
                r = 1;
            }
            else {
                width = 4;
                u = (int) UV_STAMINA.x + 5;
                r = 5;
            }

            int pY = top - (hp / 20 * rowHeight) + jiggle;
            if (shouldRenderOnOneRow)
                pY = top + jiggle;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(texture, right + (hp / 2 * 8) + r - (hp / 20 * 80), pY, u, v, width, height, 9, 9);
            RenderSystem.disableBlend();
        }
        ClientUtils.resetRenderColor();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
        if (!this.isEnabled())
            return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer playerEntity = mc.player;
        if (playerEntity == null)
            return;
        if (mc.options.renderDebug && !mc.showOnlyReducedInfo()) {
            event.getLeft().add(String.format("Stamina: %.1f/%.1f; Locked: %s", StaminaHandler.getStamina(playerEntity), StaminaHandler.getMaxStamina(playerEntity), StaminaHandler.isStaminaLocked(playerEntity)));
        }
    }
}
