package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlacedWizardInfo implements WizardInfo {
    private final Vec3d position;
    private final BlockPos blockPos;
    private final ServerWorld world;

    public PlacedWizardInfo(BlockPos blockPos, ServerWorld world) {
        this.blockPos = blockPos;
        this.world = world;
        this.position = Vec3d.of(blockPos).add(0.5, 0, 0.5);
    }

    @Override
    public @NotNull Vec3d getPosition() {
        return this.position;
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return this.getWorld().getBlockState(this.getBlockPos());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return this.getWorld().getBlockEntity(this.getBlockPos());
    }

    @Override
    public @Nullable ServerWorld getWorld() {
        return this.world;
    }
}
