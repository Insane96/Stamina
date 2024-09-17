package insane96mcp.stamina.network;

import insane96mcp.stamina.stamina.StaminaFeature;
import insane96mcp.stamina.stamina.StaminaHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StaminaSync {

    float stamina;
    boolean staminaLocked;

    public StaminaSync(float stamina, boolean staminaLocked) {
        this.stamina = stamina;
        this.staminaLocked = staminaLocked;
    }

    public static void encode(StaminaSync pkt, FriendlyByteBuf buf) {
        buf.writeFloat(pkt.stamina);
        buf.writeBoolean(pkt.staminaLocked);
    }

    public static StaminaSync decode(FriendlyByteBuf buf) {
        return new StaminaSync(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(final StaminaSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkHelper.getSidedPlayer(ctx.get()).getPersistentData().putFloat(StaminaFeature.STAMINA, message.stamina);
            NetworkHelper.getSidedPlayer(ctx.get()).getPersistentData().putBoolean(StaminaFeature.STAMINA_LOCKED, message.staminaLocked);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(ServerPlayer player) {
        Object msg = new StaminaSync(StaminaHandler.getStamina(player), StaminaHandler.isStaminaLocked(player));
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
