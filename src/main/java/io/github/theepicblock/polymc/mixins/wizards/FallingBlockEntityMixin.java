package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements WatchListener {
    @Shadow private BlockState block;

    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>((map) -> {
        if (!(world instanceof ServerWorld)) return null;

        BlockPoly poly = map.getBlockPoly(this.block.getBlock());
        if (poly != null && poly.hasWizard()) {
            return poly.createWizard((ServerWorld)this.world, this.getPos(), Wizard.WizardState.FALLING_BLOCK);
        }
        return null;
    });

    public FallingBlockEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private void onFirstTick(CallbackInfo ci) {
        //When a falling block falls. The block is actually removed by the falling block entity on the first tick.
        PolyMapMap<Wizard> previousWizards = WizardView.removeWizards(this.world, this.getBlockPos(), true);
        previousWizards.forEach((polyMap, wizard) -> {
            wizard.setState(Wizard.WizardState.FALLING_BLOCK);
        });
        this.wizards.putAll(previousWizards);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard != null) wizard.updatePosition(this.getPos());
        }));
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
    }

    @Override
    public void setRemoved(RemovalReason reason) {
        super.setRemoved(reason);
    }

    @Inject(method = "onStartedTrackingBy(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("RETURN"))
    private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        this.addPlayer(player);
    }

    @Inject(method = "onStoppedTrackingBy(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("RETURN"))
    private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        this.removePlayer(player);
    }

    @Inject(method = "setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V", at = @At("RETURN"))
    private void onRemove(CallbackInfo ci) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard != null) wizard.onRemove();
        }));
    }

    @Override
    public void addPlayer(ServerPlayerEntity playerEntity) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard != null) wizard.addPlayer(playerEntity);
        }));
    }

    @Override
    public void removePlayer(ServerPlayerEntity playerEntity) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard != null) wizard.removePlayer(playerEntity);
        }));
    }

    @Override
    public void removeAllPlayers() {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard != null) wizard.removeAllPlayers();
        }));
    }
}
