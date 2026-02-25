package insane96mcp.stamina.data.mpr;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.util.modifiable.MPRRange;
import insane96mcp.stamina.feature.StaminaHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Type;

@JsonAdapter(SStaminaCondition.Serializer.class)
public class SStaminaCondition extends MPRCondition {
    MPRRange range;
    boolean flat;

    public SStaminaCondition(MPRRange range, boolean flat, boolean inverted) {
        super(inverted);
        this.range = range;
        this.flat = flat;
    }

    protected boolean conditionCheck(LivingEntity living) {
        if (!(living instanceof ServerPlayer player))
            return false;
        float stamina = StaminaHandler.getStamina(player);
        if (this.flat)
            return this.range.isBetween(player, stamina);
        return this.range.isBetween(player, stamina / StaminaHandler.getMaxPossibleStamina(player));
    }

    public static class Serializer implements JsonDeserializer<SStaminaCondition>, JsonSerializer<SStaminaCondition> {
        public SStaminaCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new SStaminaCondition(GsonHelper.getAsObject(jObject, "range", null, context, MPRRange.class),
                    GsonHelper.getAsBoolean(jObject, "flat", false),
                    MPRCondition.deserializeInverted(jObject));
        }

        public JsonElement serialize(SStaminaCondition src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("range", context.serialize(src.range));
            if (src.flat)
                jObject.addProperty("flat", true);
            return src.endSerialization(jObject);
        }
    }
}