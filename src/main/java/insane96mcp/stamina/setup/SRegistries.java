package insane96mcp.stamina.setup;

import com.mojang.serialization.MapCodec;
import insane96mcp.stamina.Stamina;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class SRegistries {
    public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

    public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(BuiltInRegistries.ATTRIBUTE);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = createRegistry(BuiltInRegistries.MOB_EFFECT);
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPES = createRegistry(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE);
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES = createRegistry(BuiltInRegistries.LOOT_CONDITION_TYPE);

    private static <R> DeferredRegister<R> createRegistry(Registry<R> registry) {
        DeferredRegister<R> register = DeferredRegister.create(registry, Stamina.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }
}
