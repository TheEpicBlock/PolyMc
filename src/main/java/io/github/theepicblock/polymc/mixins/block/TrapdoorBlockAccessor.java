package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.TrapdoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrapdoorBlock.class)
public interface TrapdoorBlockAccessor {
    @Accessor
    BlockSetType getBlockSetType();
}
