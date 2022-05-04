package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.mixins.wizards.PistonBlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PistonWizardInfo implements WizardInfo {
    private final PistonBlockEntity be;

    public PistonWizardInfo(PistonBlockEntity be) {
        this.be = be;
    }

    @Override
    public @NotNull Vec3d getPosition() {
        var accessor = (PistonBlockEntityAccessor)be;
        var d = accessor.callGetAmountExtended(accessor.getProgress());

        return Vec3d.of(be.getPos()).add(
                0.5+d*be.getFacing().getOffsetX(),
                d*be.getFacing().getOffsetY(),
                0.5+d*be.getFacing().getOffsetZ());
    }

    @Override
    public @NotNull Vec3d getPosition(UpdateInfo info) {
        var accessor = (PistonBlockEntityAccessor)be;
        var d = accessor.callGetAmountExtended(be.getProgress(info.getTickDelta()));  // TODO ensure that the progress of the piston is threadsafe

        return Vec3d.of(be.getPos()).add(
                0.5+d*be.getFacing().getOffsetX(),
                d*be.getFacing().getOffsetY(),
                0.5+d*be.getFacing().getOffsetZ());
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        return be.getPos();
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return be.getPushedBlock();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public @Nullable ServerWorld getWorld() {
        return (ServerWorld)be.getWorld();
    }
}
