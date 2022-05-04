package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityWizardInfo implements WizardInfo {
    protected final Entity source;

    public EntityWizardInfo(Entity source) {
        this.source = source;
    }

    @Override
    public @NotNull Vec3d getPosition() {
        return source.getPos();
    }

    @Override
    public @NotNull Vec3d getPosition(UpdateInfo info) {
        var d = info.getTickDelta();
        var d1 = 1-d;
        return new Vec3d(
                source.prevX * d1 + source.getX() * d,
                source.prevY * d1 + source.getY() * d,
                source.prevZ * d1 + source.getZ() * d
        );
    }

    @Override
    public @Nullable BlockPos getBlockPos() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable BlockState getBlockState() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        // Doesn't make sense for an entity
        return null;
    }

    @Override
    public @Nullable ServerWorld getWorld() {
        return (ServerWorld)source.getWorld();
    }
}
