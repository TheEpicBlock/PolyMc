package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.poly.wizard.FallingBlockWizardInfo;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements WatchListener {
    @Shadow private BlockState block;

    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>((map) -> {
        if (!(world instanceof ServerWorld)) return null;

        var block = this.block.getBlock();
        var poly = map.getBlockPoly(block);
        if (poly != null && poly.hasWizard()) {
            try {
                return poly.createWizard(new FallingBlockWizardInfo((FallingBlockEntity)(Object)this));
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to create block wizard for "+block.getTranslationKey()+" | "+poly);
                t.printStackTrace();
            }
        }
        return null;
    });

    public FallingBlockEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "spawnFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onSpawnFromBlock(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir, FallingBlockEntity entity) {
        //When a falling block falls. The block is actually removed by the falling block entity on the first tick.
        PolyMapMap<Wizard> previousWizards = WizardView.removeWizards(world, pos, true);
        previousWizards.forEach((polyMap, wizard) -> {
            wizard.changeInfo(new FallingBlockWizardInfo(entity));
        });
        ((FallingBlockEntityMixin)(Object)entity).wizards.putAll(previousWizards);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        wizards.forEach(((polyMap, wizard) -> {
            if (wizard == null) return;
            wizard.onMove(); // It is assumed that sand is constantly falling
            wizard.onTick();
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
