package insane96mcp.stamina.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.stamina.feature.StaminaHandler;
import insane96mcp.stamina.network.StaminaSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * Enchantment entity effect that consumes the player's stamina (e.g. used by Lunge).
 * Pair it with {@link HasStaminaCondition} in the requirements to prevent the effect when there's not enough stamina.
 */
public record ConsumeStaminaEffect(LevelBasedValue amount) implements EnchantmentEntityEffect {
    public static final MapCodec<ConsumeStaminaEffect> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(ConsumeStaminaEffect::amount)).apply(i, ConsumeStaminaEffect::new)
    );

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        if (!(entity instanceof ServerPlayer player)
                || !StaminaHandler.usesStamina(player))
            return;

        StaminaHandler.consumeStamina(player, this.amount.calculate(enchantmentLevel));
        StaminaSync.sync(player);
    }

    @Override
    public MapCodec<ConsumeStaminaEffect> codec() {
        return CODEC;
    }
}
