package io.github.theepicblock.polymc.api.block;

import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BlockStatePoly(@NotNull BlockState vanillaBlock, @Nullable WizardConstructor wizard) {

}
