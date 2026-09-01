package insane96mcp.stamina.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.event.PlayerSprintEvent;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.mixin.GuiAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Stamina.MOD_ID, value = Dist.CLIENT)
public class StaminaFeatureClient {
    private static final Vec2 UV_STAMINA = new Vec2(0, 9);

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, Stamina.location(StaminaFeature.OVERLAY), (guiGraphics, partialTick) -> {
            Minecraft mc = Minecraft.getInstance();
            if (Feature.isEnabled(StaminaFeature.class) && mc.gameMode != null && mc.gameMode.canHurtPlayer() && !mc.options.hideGui)
                renderStamina(mc.gui, guiGraphics);
        });
    }

    public static void renderStamina(Gui gui, GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        ((GuiAccessor) gui).getRandom().setSeed(gui.getGuiTicks() * 312871L);

        boolean shouldRenderOnOneRow = ModList.get().isLoaded("mantle");

        int health = Mth.ceil(player.getHealth());
        if (StaminaFeature.stamina$boundToMaxHealth)
            health = Mth.ceil(player.getMaxHealth());

        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float) attrMaxHealth.getValue(), health);
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
        if (absorp > 0) {
            if (shouldRenderOnOneRow)
                leftHeight -= 10;
        }

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

        ResourceLocation texture = StaminaFeature.HEART_OVERLAY;
        if (StaminaHandler.isStaminaLocked(player))
            texture = StaminaFeature.LOCKED_HEART_OVERLAY;

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

    @SubscribeEvent
    public static void onSprint(PlayerSprintEvent event) {
        if (!Feature.isEnabled(StaminaFeature.class)
                || event.getPlayer().getAbilities().instabuild)
            return;

        if (!StaminaHandler.canSprint(event.getPlayer()) || (StaminaFeature.disable$sprinting && !event.getPlayer().canStartSwimming()) || (StaminaFeature.disable$swimming && event.getPlayer().canStartSwimming()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
        if (!Feature.isEnabled(StaminaFeature.class))
            return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer playerEntity = mc.player;
        if (playerEntity == null)
            return;
        if (mc.getDebugOverlay().showDebugScreen() && !mc.showOnlyReducedInfo()) {
            event.getLeft().add(String.format("Stamina: %.1f/%.1f; Locked: %s", StaminaHandler.getStamina(playerEntity), StaminaHandler.getMaxStamina(playerEntity), StaminaHandler.isStaminaLocked(playerEntity)));
        }
    }
}
