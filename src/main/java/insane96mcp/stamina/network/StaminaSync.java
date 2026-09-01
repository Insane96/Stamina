package insane96mcp.stamina.network;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.feature.StaminaFeature;
import insane96mcp.stamina.feature.StaminaHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public record StaminaSync(float stamina, boolean staminaLocked) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StaminaSync> TYPE =
            new CustomPacketPayload.Type<>(Stamina.location("stamina_sync"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, StaminaSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaminaSync::stamina,
            ByteBufCodecs.BOOL, StaminaSync::staminaLocked,
            StaminaSync::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final StaminaSync payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null)
                return;
            ModNBTData.put(player, StaminaFeature.STAMINA, payload.stamina());
            ModNBTData.put(player, StaminaFeature.STAMINA_LOCKED, payload.staminaLocked());
        });
    }

    public static void sync(ServerPlayer player) {
        if (!NetworkRegistry.hasChannel(player.connection, TYPE.id()))
            return;
        PacketDistributor.sendToPlayer(player, new StaminaSync(StaminaHandler.getStamina(player), StaminaHandler.isStaminaLocked(player)));
    }
}
