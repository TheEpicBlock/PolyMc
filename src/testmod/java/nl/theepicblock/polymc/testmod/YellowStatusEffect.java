package nl.theepicblock.polymc.testmod;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class YellowStatusEffect extends StatusEffect {
    /**
     * Helpful for automatic testing, to simulate the fact that
     * this status effect will not be a registered one on the client
     */
    public static boolean SIMULATE_UNAVAILABLE = false;

    protected YellowStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public int getColor() {
        return SIMULATE_UNAVAILABLE ? 0 : super.getColor();
    }

    @Override
    public String getTranslationKey() {
        return SIMULATE_UNAVAILABLE ? "translation.unavailable" : super.getTranslationKey();
    }
}
