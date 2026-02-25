package insane96mcp.stamina.data.mpr;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;
import insane96mcp.mobspropertiesrandomness.data.json.util.modifiable.MPRRange;
import insane96mcp.stamina.feature.StaminaHandler;
import insane96mcp.stamina.network.StaminaSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(SAddProperty.Serializer.class)
public class SAddProperty extends MPRProperty {
    MPRRange amount;
    boolean flat;

    public SAddProperty(MPRRange amount, boolean flat, List<MPRCondition> conditions) {
        super(conditions);
        this.amount = amount;
        this.flat = flat;
    }

    @Override
    public boolean apply(LivingEntity living) {
        if (!(living instanceof ServerPlayer player))
            return false;
        float stamina = (float) this.amount.getDoubleBetween(player);
        if (stamina >= 0) {
            if (this.flat)
                StaminaHandler.regenStamina(player, stamina);
            else
                StaminaHandler.regenStamina(player, StaminaHandler.getMaxStamina(player) * stamina);
        }
        else {
            if (this.flat)
                StaminaHandler.consumeStamina(player, -stamina);
            else
                StaminaHandler.consumeStamina(player, StaminaHandler.getMaxStamina(player) * -stamina);
        }
        StaminaSync.sync(player);
        return true;
    }

    public static class Serializer implements JsonDeserializer<SAddProperty>, JsonSerializer<SAddProperty> {
        @Override
        public SAddProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new SAddProperty(GsonHelper.getAsObject(jObject, "amount", null, context, MPRRange.class),
                    GsonHelper.getAsBoolean(jObject, "flat", false), MPRCondition.deserializeConditions(jObject, context));
        }

        @Override
        public JsonElement serialize(SAddProperty src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("amount", context.serialize(src.amount));
            if (src.flat)
                jObject.addProperty("flat", true);
            return src.endSerialization(jObject, context);
        }
    }
}
