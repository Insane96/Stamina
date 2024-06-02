package insane96mcp.stamina.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class SEventFactory {
    public static float onStaminaConsumed(Player player, float amount) {
        StaminaEvent.Consumed event = new StaminaEvent.Consumed(player, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onStaminaRegenerated(Player player, float amount) {
        StaminaEvent.Regenerated event = new StaminaEvent.Regenerated(player, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }
}
