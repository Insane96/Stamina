package insane96mcp.stamina.stamina;

public interface IStaminaModifier {
    float consumedStaminaModifier(int amplifier);
    float regenStaminaModifier(int amplifier);
    int bonusMaxStamina(int amplifier);
}
