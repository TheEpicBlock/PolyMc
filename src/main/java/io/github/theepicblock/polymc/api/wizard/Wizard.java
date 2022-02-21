package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Wizard implements WatchListener {
    private WizardInfo info;

    public Wizard(WizardInfo info) {
        this.info = info;
    }

    /**
     * Switches this wizard's info. This can be used instead of creating a new wizard
     */
    public final void changeInfo(WizardInfo newInfo) {
        this.info = newInfo;
    }

    public void onRemove() {
        this.removeAllPlayers();
    }

    @Override
    public void removeAllPlayers() {
        //Default implementation.
        this.getPlayersWatchingChunk().forEach(this::removePlayer);
    }

    public void onMove() {}

    public void onTick() {}

    public boolean needsTicking() {
        return false;
    }

    /**
     * @return the bottom center of this wizard's location. For a block this would be for example 11.5, 3, 45.5
     */
    public @NotNull Vec3d getPosition() {
        return info.getPosition();
    }

    /**
     * Retrieves the {@link BlockState} of the block this wizard is occupying.
     */
    public @Nullable BlockState getBlockState() {
        return info.getBlockState();
    }
    /**
     * Attempts to retrieve the current block pos of this wizard
     */
    public @Nullable BlockPos getBlockPos() {
        return info.getBlockPos();
    }

    /**
     * Retrieves the {@link BlockEntity} of the block this wizard is occupying.
     */
    public @Nullable BlockEntity getBlockEntity() {
        return info.getBlockEntity();
    }

    public @Nullable ServerWorld getWorld() {
        return info.getWorld();
    }

    public List<ServerPlayerEntity> getPlayersWatchingChunk() {
        return this.getWorld().getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(
                new ChunkPos((int)this.getPosition().x >> 4, (int)this.getPosition().z >> 4), false);
    }
}
