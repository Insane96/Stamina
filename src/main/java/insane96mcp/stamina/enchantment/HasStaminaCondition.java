package insane96mcp.stamina.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.stamina.feature.StaminaHandler;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Enchantment requirement that passes if the player has at least {@code amount} stamina and their stamina is not locked.
 * Always passes for non-players and for players that don't use stamina (creative, spectator).
 */
public record HasStaminaCondition(LevelBasedValue amount) implements LootItemCondition {
    public static final MapCodec<HasStaminaCondition> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter(HasStaminaCondition::amount)).apply(i, HasStaminaCondition::new)
    );

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Player player)
                || !StaminaHandler.usesStamina(player))
            return true;

        float amount = this.amount.calculate(lootContext.getParameter(LootContextParams.ENCHANTMENT_LEVEL));
        return !StaminaHandler.isStaminaLocked(player) && StaminaHandler.getStamina(player) >= amount;
    }

    @Override
    public MapCodec<HasStaminaCondition> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY, LootContextParams.ENCHANTMENT_LEVEL);
    }
}
