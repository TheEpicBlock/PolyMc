package io.github.theepicblock.polymc.mixins.wizards.block;

import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {
    @Accessor
    Palette<T> getPalette();

    @Accessor
    PackedIntegerArray getData();
}
