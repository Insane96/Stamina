package insane96mcp.stamina.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccesor {
    @Accessor
    int getDisplayHealth();
    @Accessor
    RandomSource getRandom();
}
