package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.poly.wizard.PistonWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class MixinPistonBlockEntity extends BlockEntity {
    public MixinPistonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow private BlockState pushedBlock;

    @Shadow public abstract BlockState getPushedBlock();

    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>((map) -> {
        if (!(world instanceof ServerWorld)) return null;

        var block = this.getPushedBlock().getBlock();
        var poly = map.getBlockPoly(block);
        if (poly != null && poly.hasWizard()) {
            try {
                return poly.createWizard(new PistonWizardInfo((PistonBlockEntity)(Object)this));
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to create block wizard for "+block.getTranslationKey()+" | "+poly);
                t.printStackTrace();
            }
        }
        return null;
    });

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
    }

    @Inject(method = "setWorld(Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void onInit(World world, CallbackInfo ci) {
        if (!world.isClient) {
            var allPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)world, this.getPos());
            allPlayers.forEach((player) -> {
                Wizard wiz = wizards.get(PolyMapProvider.getPolyMap(player));
            });
            wizards.forEach(((polyMap, wizard) -> {
                if (wizard == null) return;
                var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                wizard.addPlayer(filteredView);
                filteredView.sendBatched();
            }));
        }
    }

    @Inject(method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/PistonBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V"))
    private static void onTick(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci) {
        MixinPistonBlockEntity be = (MixinPistonBlockEntity)(Object)blockEntity;
        if (be == null) return;

        var allNearbyPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)be.getWorld(), be.getPos());
        be.wizards.forEach((polyMap, wizard) -> {
            if (wizard == null) return;
            var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
            wizard.onMove(filteredView); // Pistons move constantly
            wizard.onTick(filteredView);
            filteredView.sendBatched();
        });
    }

    @Inject(method = "markRemoved()V", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        if (!(this.getWorld() instanceof ServerWorld)) return;
        var allNearbyPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)this.getWorld(), this.getPos());
        wizards.forEach((polyMap, wizard) -> {
            var filteredView = new PolyMapFilteredPlayerView(allNearbyPlayers, polyMap);
            if (wizard != null) wizard.onRemove(filteredView);
            filteredView.sendBatched();
        });
    }
}
