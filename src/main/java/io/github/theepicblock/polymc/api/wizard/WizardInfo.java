package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WizardInfo {
    @NotNull Vec3d getPosition();

    @NotNull Vec3d getPosition(UpdateInfo info);

    @Nullable BlockPos getBlockPos();

    @Nullable BlockState getBlockState();

    @Nullable BlockEntity getBlockEntity();

    @Nullable ServerWorld getWorld();
}
