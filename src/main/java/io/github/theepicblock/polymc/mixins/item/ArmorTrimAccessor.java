package io.github.theepicblock.polymc.mixins.item;

import net.minecraft.item.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorTrim.class)
public interface ArmorTrimAccessor {
    @Accessor
    boolean isShowInTooltip();
}
