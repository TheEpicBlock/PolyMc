package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.block.SlabBlock;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlabBlock.class)
public interface SlabBlockAccessor {
    @Accessor
    static VoxelShape getBOTTOM_SHAPE() {
        throw new IllegalStateException();
    }
}
