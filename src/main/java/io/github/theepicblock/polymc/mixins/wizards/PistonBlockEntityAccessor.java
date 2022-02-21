package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PistonBlockEntity.class)
public interface PistonBlockEntityAccessor {
    @Invoker
    float callGetAmountExtended(float progress);

    @Accessor
    float getProgress();
}
