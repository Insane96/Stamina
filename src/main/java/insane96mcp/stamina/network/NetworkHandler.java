package insane96mcp.stamina.network;

import insane96mcp.stamina.Stamina;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(3);
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Stamina.MOD_ID, "network_channel"))
			.clientAcceptedVersions(s -> true)
			.serverAcceptedVersions(s -> true)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	private static int index = 0;

	public static void init() {
		CHANNEL.registerMessage(++index, StaminaSync.class, StaminaSync::encode, StaminaSync::decode, StaminaSync::handle);
	}
}
