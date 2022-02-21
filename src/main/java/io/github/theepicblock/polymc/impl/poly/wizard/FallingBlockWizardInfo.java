package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FallingBlockWizardInfo implements WizardInfo {
    private final FallingBlockEntity entity;

    public FallingBlockWizardInfo(FallingBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public @NotNull Vec3d getPosition() {
        return entity.getPos();
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return null;
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return entity.getBlockState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public @Nullable ServerWorld getWorld() {
        return (ServerWorld)entity.getWorld();
    }
}
