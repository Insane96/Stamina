package insane96mcp.stamina.feature;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.event.PlayerSprintEvent;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.mixin.GuiAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
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
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
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

    public static void renderStamina(Gui gui, GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        // Same seed as vanilla Gui#extractHealthLevel (int multiplication, overflow included) so the low health shake matches the hearts
        ((GuiAccessor) gui).getRandom().setSeed(gui.getGuiTicks() * 312871);

        boolean shouldRenderOnOneRow = ModList.get().isLoaded("mantle");

        int health = Mth.ceil(player.getHealth());
        // Half hearts the stamina is spread over
        int staminaHealth = health;
        if (StaminaFeature.stamina$boundToMaxHealth)
            staminaHealth = Mth.ceil(player.getMaxHealth());

        // Same as vanilla Gui#extractHealthLevel, so rows, regen and shake match the hearts
        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = Math.max((float) attrMaxHealth.getValue(), (float) Math.max(((GuiAccessor) gui).getDisplayHealth(), health));
        int hearts = Mth.ceil(healthMax / 2f);
        int absorp = Mth.ceil(player.getAbsorptionAmount());
        int absorpHearts = Mth.ceil(absorp / 2f);
        boolean lowHealth = health + absorp <= 4;

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
        float maxStamina = StaminaHandler.getMaxStamina(player);
        if (maxStamina <= 0 || staminaHealth <= 0)
            return;
        float staminaPerHalfHeart = maxStamina / staminaHealth;
        if (shouldRenderOnOneRow)
            staminaPerHalfHeart = maxStamina / Math.min(staminaHealth, 20f);
        int halfHeartsMaxStamina = Mth.ceil(maxStamina / staminaPerHalfHeart);
        int halfHeartsStamina = Mth.ceil(StaminaHandler.getStamina(player) / staminaPerHalfHeart);
        int height = 9;
        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION))
            regen = gui.getGuiTicks() % Mth.ceil(healthMax + 5.0F);

        Identifier texture = StaminaFeature.HEART_OVERLAY;
        if (StaminaHandler.isStaminaLocked(player))
            texture = StaminaFeature.LOCKED_HEART_OVERLAY;

        // Vanilla draws the absorption hearts first, consuming the random for each of them when shaking
        if (lowHealth) {
            for (int a = 0; a < absorpHearts; a++)
                ((GuiAccessor) gui).getRandom().nextInt(2);
        }

        for (int heart = hearts - 1; heart >= 0; heart--) {
            //Doesn't work with absorption ...
            int jiggle = 0;
            if (lowHealth)
                jiggle += ((GuiAccessor) gui).getRandom().nextInt(2);
            if (heart == regen)
                jiggle -= 2;
            for (int hp = heart * 2 + 1; hp >= heart * 2; hp--) {
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
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, right + (hp / 2 * 8) + r - (hp / 20 * 80), pY, u, v, width, height, 9, 9);
            }
        }
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
    public static void registerDebugEntries(RegisterDebugEntriesEvent event) {
        event.register(StaminaFeature.STAMINA, STAMINA_DEBUG_ENTRY);
        event.includeInProfile(StaminaFeature.STAMINA, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
    }

    private static final DebugScreenEntry STAMINA_DEBUG_ENTRY = (displayer, serverOrClientLevel, clientChunk, serverChunk) -> {
        if (!Feature.isEnabled(StaminaFeature.class))
            return;
        LocalPlayer playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;
        displayer.addLine(String.format("Stamina: %.1f/%.1f; Locked: %s", StaminaHandler.getStamina(playerEntity), StaminaHandler.getMaxStamina(playerEntity), StaminaHandler.isStaminaLocked(playerEntity)));
    };
}
