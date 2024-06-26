package insane96mcp.stamina.network;

import insane96mcp.stamina.stamina.StaminaFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StaminaSync {

    int stamina;
    boolean staminaLocked;

    public StaminaSync(int stamina, boolean staminaLocked) {
        this.stamina = stamina;
        this.staminaLocked = staminaLocked;
    }

    public static void encode(StaminaSync pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.stamina);
        buf.writeBoolean(pkt.staminaLocked);
    }

    public static StaminaSync decode(FriendlyByteBuf buf) {
        return new StaminaSync(buf.readInt(), buf.readBoolean());
    }

    public static void handle(final StaminaSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkHelper.getSidedPlayer(ctx.get()).getPersistentData().putInt(StaminaFeature.STAMINA, message.stamina);
            NetworkHelper.getSidedPlayer(ctx.get()).getPersistentData().putBoolean(StaminaFeature.STAMINA_LOCKED, message.staminaLocked);
        });
        ctx.get().setPacketHandled(true);
    }
}
