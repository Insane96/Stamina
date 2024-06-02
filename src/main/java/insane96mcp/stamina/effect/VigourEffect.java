package insane96mcp.stamina.effect;

import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.stamina.stamina.IStaminaModifier;
import insane96mcp.stamina.stamina.StaminaFeature;
import net.minecraft.world.effect.MobEffectCategory;

public class VigourEffect extends ILMobEffect implements IStaminaModifier {
    public VigourEffect(MobEffectCategory typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn, false);
    }

    @Override
    public float consumedStaminaModifier(int amplifier) {
        /*int lvl = amplifier + 1;
        return (lvl * -0.10f - 0.10f);*/
        return 0f;
    }

    @Override
    public float regenStaminaModifier(int amplifier) {
        return 0f;
    }

    @Override
    public int bonusMaxStamina(int amplifier) {
        int lvl = amplifier + 1;
        return (lvl * StaminaFeature.staminaPerLevelOfVigourEffect);
    }
}
