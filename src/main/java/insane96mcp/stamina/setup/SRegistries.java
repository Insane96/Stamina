package insane96mcp.stamina.setup;

import insane96mcp.stamina.Stamina;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class SRegistries {
    public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

    public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(BuiltInRegistries.ATTRIBUTE);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = createRegistry(BuiltInRegistries.MOB_EFFECT);

    private static <R> DeferredRegister<R> createRegistry(Registry<R> registry) {
        DeferredRegister<R> register = DeferredRegister.create(registry, Stamina.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }
}
