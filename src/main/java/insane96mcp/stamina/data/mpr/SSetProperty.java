package insane96mcp.stamina.data.mpr;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;
import insane96mcp.mobspropertiesrandomness.data.json.util.modifiable.MPRRange;
import insane96mcp.stamina.network.StaminaSync;
import insane96mcp.stamina.stamina.StaminaHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(SSetProperty.Serializer.class)
public class SSetProperty extends MPRProperty {
    MPRRange range;
    boolean flat;

    public SSetProperty(MPRRange range, boolean flat, List<MPRCondition> conditions) {
        super(conditions);
        this.range = range;
        this.flat = flat;
    }

    @Override
    public boolean apply(LivingEntity living) {
        if (!(living instanceof ServerPlayer player))
            return false;
        float stamina = (float) this.range.getDoubleBetween(player);
        if (this.flat)
            StaminaHandler.setStamina(player, stamina);
        else
            StaminaHandler.setStamina(player, StaminaHandler.getMaxStamina(player) * stamina);
        StaminaSync.sync(player);
        return true;
    }

    public static class Serializer implements JsonDeserializer<SSetProperty>, JsonSerializer<SSetProperty> {
        @Override
        public SSetProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new SSetProperty(GsonHelper.getAsObject(jObject, "range", null, context, MPRRange.class),
                    GsonHelper.getAsBoolean(jObject, "flat", false), MPRCondition.deserializeConditions(jObject, context));
        }

        @Override
        public JsonElement serialize(SSetProperty src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("range", context.serialize(src.range));
            if (src.flat)
                jObject.addProperty("flat", true);
            return src.endSerialization(jObject, context);
        }
    }
}
