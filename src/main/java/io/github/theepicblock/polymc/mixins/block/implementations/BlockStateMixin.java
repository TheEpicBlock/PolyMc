package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.mixin.BlockStateDuck;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public class BlockStateMixin implements BlockStateDuck {
    @Unique
    private boolean isVanilla = false;
    @Override
    public void polymc$setVanilla(boolean value) {
        this.isVanilla = value;
    }

    @Override
    public boolean polymc$getVanilla() {
        return this.isVanilla;
    }
}
