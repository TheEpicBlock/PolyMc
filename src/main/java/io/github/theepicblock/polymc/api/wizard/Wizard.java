package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public abstract class Wizard implements WatchListener {
    private final ServerWorld world;
    private Vec3d position;
    private WizardState state;

    public Wizard(ServerWorld world, Vec3d position, WizardState state) {
        this.world = world;
        this.position = position;
        this.state = state;
    }

    public void onRemove() {
        this.removeAllPlayers();
    }

    public void onMove() {}

    public void onStateChange() {}

    @Override
    public void removeAllPlayers() {
        //Default implementation.
        this.getPlayersWatchingChunk().forEach(this::removePlayer);
    }

    public final ServerWorld getWorld() {
        return world;
    }

    public final Vec3d getPosition() {
        return position;
    }

    public final void updatePosition(Vec3d position) {
        this.position = position;
        this.onMove();
    }

    public final WizardState getState() {
        return state;
    }

    public final void setState(WizardState state) {
        this.state = state;
        this.onStateChange();
    }

    /**
     * Attempts to retrieve the current block pos of this wizard
     * @return the position as a block position
     * @throws IllegalStateException if the wizard is not in a grid
     */
    public BlockPos getBlockPos() {
        if (!this.getState().isStatic())
            throw new IllegalStateException("attempted to access block pos of non-static wizard");
        return new BlockPos(position);
    }

    /**
     * Retrieves the {@link BlockState} of the block this wizard is occupying.
     * @return a block state
     * @throws IllegalStateException if the wizard is not in a grid
     */
    public BlockState getBlockState() {
        return this.getWorld().getBlockState(this.getBlockPos());
    }

    /**
     * Retrieves the {@link BlockEntity} of the block this wizard is occupying.
     * @return a block entity, may be null if there is none at this location
     * @throws IllegalStateException if the wizard is not in a grid
     */
    public BlockEntity getBlockEntity() {
        return this.getWorld().getBlockEntity(this.getBlockPos());
    }

    /**
     * Tries to retrieve the {@link BlockEntity} of the block this wizard is occupying.
     * This method will return null instead of an {@link IllegalStateException} if the wizard is not in a grid.
     * @return a block entity, may be null if there is none at this location or if the wizard is not in a grid.
     */
    public BlockEntity tryGetBlockEntity() {
        try {
            return this.getWorld().getBlockEntity(this.getBlockPos());
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    public List<ServerPlayerEntity> getPlayersWatchingChunk() {
        return this.getWorld().getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(
                new ChunkPos((int)this.getPosition().x >> 4, (int)this.getPosition().z >> 4), false);
    }

    public enum WizardState {
        /**
         * The wizard is a block in the world
         */
        BLOCK,
        /**
         * The wizard is attached to a falling block entity
         */
        FALLING_BLOCK,
        /**
         * The wizard is being moved by a piston
         */
        MOVING_PISTON,
        /**
         * The wizard is in a miscellaneous state that is not confined to a grid.
         */
        MISC_MOVING;

        /**
         * Checks whether the state is confined to a grid or not.
         * If the state is static, {@link Wizard#position} will always be in the center of a block
         */
        private boolean isStatic() {
            return this == BLOCK;
        }
    }
}
