package insane96mcp.stamina.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(StaminaSync.TYPE, StaminaSync.STREAM_CODEC, StaminaSync::handle);
    }
}
