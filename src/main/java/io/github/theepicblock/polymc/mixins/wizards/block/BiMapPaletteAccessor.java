package io.github.theepicblock.polymc.mixins.wizards.block;

import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.world.chunk.BiMapPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiMapPalette.class)
public interface BiMapPaletteAccessor<T> {
    @Accessor
    Int2ObjectBiMap<T> getMap();
}
