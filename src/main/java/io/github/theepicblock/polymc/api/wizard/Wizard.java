package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater;
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


    public abstract void addPlayer(PacketConsumer player);

    public abstract void removePlayer(PacketConsumer player);

    public void onRemove(PacketConsumer players) {
        this.removeAllPlayers(players);
    }

    public void removeAllPlayers(PacketConsumer players) {
        //Default implementation.
        this.removePlayer(players);
    }

    /**
     * Called when this Wizard moves its location. For block polys, this will be when the block is pushed with a piston,
     * is inside a sand entity, or some other mod moves it in some other way.
     * For entities, you might want to take a look at using {@link #update(PacketConsumer, UpdateInfo)} instead,
     * depending on your needs.
     */
    public void onMove(PacketConsumer players) { this.onMove(); }

    /**
     * @deprecated use {@link #onMove(PacketConsumer)}
     */
    @Deprecated
    public void onMove() {}

    /**
     * This function is called every tick, as long as {@link #needsTicking()} is true.
     * This function is called on the main thread, it's recommended to do any packet sending inside of {@link #update(PacketConsumer, UpdateInfo)}
     */
    public void onTick(PacketConsumer players) { this.onTick(); }

    /**
     * This function is called every tick, as long as {@link #needsTicking()} is true.
     * This function is called on the main thread, it's recommended to do any packet sending inside of {@link #update(PacketConsumer, UpdateInfo)}
     * @deprecated use {@link #onTick(PacketConsumer)}
     */
    @Deprecated
    public void onTick() {}

    /**
     * This method can be called off-thread and can be called more than 20 times per second. This is the preferred method to send packets from, although multithreading might get in the way.
     * Take care to ensure everything you do here is thread safe, you can do syncing inside {@link #onTick(PacketConsumer)}. If it's too hard to make your logic threadsafe, just use {@link #onTick(PacketConsumer)}, it's probably not worth it.
     *
     * You can use {@link io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater.Safe} and {@link io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater.Unsafe} to get an idea of what's safe/unsafe to call.
     */
    @ApiStatus.Experimental
    public void update(PacketConsumer players, UpdateInfo info) {}

    public boolean needsTicking() {
        return false;
    }

    /**
     * @return the bottom center of this wizard's location. For a block this would be for example 11.5, 3, 45.5
     */
    @ThreadedWizardUpdater.Unsafe // Purely for if I want to do some weird stuff with this method later
    public @NotNull Vec3d getPosition() {
        return info.getPosition();
    }

    /**
     * @return the bottom center of this wizard's location. For a block this would be for example 11.5, 3, 45.5
     */
    @ApiStatus.Experimental
    @ThreadedWizardUpdater.Safe
    public @NotNull Vec3d getPosition(UpdateInfo uInfo) {
        return info.getPosition(uInfo);
    }

    /**
     * Retrieves the {@link BlockState} of the block this wizard is occupying.
     */
    @ThreadedWizardUpdater.Unsafe
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
    @ThreadedWizardUpdater.Unsafe
    public @Nullable BlockEntity getBlockEntity() {
        return info.getBlockEntity();
    }

    @ThreadedWizardUpdater.KindaSafe
    public @Nullable ServerWorld getWorld() {
        return info.getWorld();
    }

    @Deprecated
    @ThreadedWizardUpdater.Unsafe
    public List<ServerPlayerEntity> getPlayersWatchingChunk() {
        return this.getWorld().getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(
                new ChunkPos((int)this.getPosition().x >> 4, (int)this.getPosition().z >> 4), false);
    }
}
