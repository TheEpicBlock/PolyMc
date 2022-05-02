package io.github.theepicblock.polymc.api.wizard;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Wizard {
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


    public abstract void addPlayer(ServerPlayerEntity playerEntity);

    public abstract void removePlayer(ServerPlayerEntity playerEntity);

    public void onRemove(PlayerView players) {
        this.removeAllPlayers(players);
    }

    public void removeAllPlayers(PlayerView players) {
        //Default implementation.
        players.forEach(this::removePlayer);
    }

    public void onMove(PlayerView players) { this.onMove(); }

    /**
     * @deprecated use {@link #onMove(PlayerView)}
     */
    @Deprecated
    public void onMove() {}

    /**
     * This function is called every tick, as long as {@link #needsTicking()} is true.
     * This function is called on the main thread, it's recommended to do any packet sending inside of {@link #update()}
     */
    public void onTick(PlayerView players) { this.onTick(); }

    /**
     * This function is called every tick, as long as {@link #needsTicking()} is true.
     * This function is called on the main thread, it's recommended to do any packet sending inside of {@link #update()}
     * @deprecated use {@link #onTick(PlayerView)}
     */
    @Deprecated
    public void onTick() {}

    /**
     * An off-thread ticking method
     */
    @ApiStatus.Experimental
    public void update(PlayerView players, UpdateInfo info) {}

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

    @Deprecated
    public List<ServerPlayerEntity> getPlayersWatchingChunk() {
        return this.getWorld().getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(
                new ChunkPos((int)this.getPosition().x >> 4, (int)this.getPosition().z >> 4), false);
    }
}
