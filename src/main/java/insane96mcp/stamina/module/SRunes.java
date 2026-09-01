package insane96mcp.stamina.module;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.runeenchanting.RuneHelper;
import insane96mcp.runeenchanting.runes.Rune;
import insane96mcp.runeenchanting.setup.RERunes;
import insane96mcp.stamina.Stamina;
import insane96mcp.stamina.feature.StaminaFeature;
import insane96mcp.stamina.setup.SEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers a Rune Enchanting equivalent for Stamina's own enchantments (Vigour, Curse of Weariness), so the
 * effect can also be obtained from a rune instead of only from the real enchantment. Only ever constructed if
 * Rune Enchanting is installed (see {@link RuneCompat} and {@link Stamina}), so this class - and therefore its
 * references to Rune Enchanting's classes - is never loaded otherwise.
 * <p>
 * These runes carry no gameplay logic of their own: {@link RuneCompat#getRuneLevel} is queried directly from
 * the same place that already reads the real enchantment level ({@link insane96mcp.stamina.feature.StaminaHandler#getMaxPossibleStamina}),
 * and the returned rune-equivalent level is simply added on top.
 */
public class SRunes {
    private static final Map<ResourceKey<Enchantment>, Rune> RUNES = new LinkedHashMap<>();

    public SRunes(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegister);
    }

    private void onRegister(RegisterEvent event) {
        event.register(RERunes.REGISTRY_KEY, helper -> {
            register(helper, SEnchantments.VIGOUR, "vigour", new VigourRune());
            register(helper, SEnchantments.CURSE_OF_WEARINESS, "curse_of_weariness", new CurseOfWearinessRune());
        });
    }

    private static void register(RegisterEvent.RegisterHelper<Rune> helper, ResourceKey<Enchantment> enchantment, String id, Rune rune) {
        helper.register(Stamina.location(id), rune);
        RUNES.put(enchantment, rune);
    }

    /**
     * Returns {@code levelEquivalent} if {@code stack} carries the rune equivalent to {@code enchantment}, 0
     * otherwise. Called only through {@link RuneCompat#getRuneLevel}, which already checked this class is safe
     * to touch.
     */
    public static int getRuneLevel(ItemStack stack, ResourceKey<Enchantment> enchantment, int levelEquivalent) {
        Rune rune = RUNES.get(enchantment);
        if (rune == null)
            return 0;

        Holder<Rune> holder = RERunes.REGISTRY.wrapAsHolder(rune);
        return RuneHelper.hasRune(stack, holder) ? levelEquivalent : 0;
    }

    public static class VigourRune extends Rune {
        @Override
        public String getName() {
            return "Vigour";
        }

        @Override
        public String getDescription() {
            return "Increases max stamina";
        }

        @Override
        public @Nullable String getInfo() {
            return "Max stamina: +%s%%";
        }

        @Override
        public MutableComponent getInfoComponent() {
            int lvl = StaminaFeature.enchantments$vigourRuneLevelEquivalent;
            return Component.translatable(getInfoTranslationKey(),
                    InsaneLib.ONE_DECIMAL_FORMATTER.format(lvl * StaminaFeature.stamina$bonusPercentagePerLevelOfVigourEnchantment * 100));
        }

        @Override
        public void addItemsToApplicableTag(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender) {}
    }

    public static class CurseOfWearinessRune extends Rune {
        @Override
        public String getName() {
            return "Curse of Weariness";
        }

        @Override
        public String getDescription() {
            return "Reduces max stamina";
        }

        @Override
        public @Nullable String getInfo() {
            return "Max stamina: -%s%%";
        }

        @Override
        public MutableComponent getInfoComponent() {
            return Component.translatable(getInfoTranslationKey(),
                    InsaneLib.ONE_DECIMAL_FORMATTER.format(StaminaFeature.stamina$reductionPercentagePerLevelOfCurseOfWeariness * 100));
        }

        @Override
        public void addItemsToApplicableTag(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender) {}
    }
}
