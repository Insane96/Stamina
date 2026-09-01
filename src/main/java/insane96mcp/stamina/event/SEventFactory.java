package insane96mcp.stamina.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

public class SEventFactory {
    public static float onStaminaConsumed(Player player, float amount) {
        StaminaEvent.Consumed event = new StaminaEvent.Consumed(player, amount);
        NeoForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onStaminaRegenerated(Player player, float amount) {
        StaminaEvent.Regenerated event = new StaminaEvent.Regenerated(player, amount);
        NeoForge.EVENT_BUS.post(event);
        return event.getAmount();
    }
}
