package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.impl.ConfigManager;
import net.minecraft.block.BlockState;

public interface BlockStateDuck {
    void polymc$setVanilla(boolean value);
    boolean polymc$getVanilla();

    static boolean isMaybeVanilla(BlockState state) {
        return !ConfigManager.getConfig().remapVanillaBlockIds || ((BlockStateDuck) state).polymc$getVanilla();
    }

    static void markVanilla(BlockState state) {
        ((BlockStateDuck) state).polymc$setVanilla(true);
    }
}
