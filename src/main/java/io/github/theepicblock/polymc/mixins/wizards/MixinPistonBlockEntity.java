package io.github.theepicblock.polymc.mixins.wizards;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class MixinPistonBlockEntity extends BlockEntity {
    @Shadow private BlockState pushedBlock;

    @Shadow protected abstract float getAmountExtended(float progress);

    @Shadow private float progress;
    @Shadow private Direction facing;
    @Unique
    private final PolyMapMap<Wizard> wizards = new PolyMapMap<>((map) -> {
        BlockPoly poly = map.getBlockPoly(this.pushedBlock.getBlock());
        if (poly != null && poly.hasWizard()) {
            return poly.createWizard(Vec3d.of(this.getPos()).add(0.5,0,0.5), Wizard.WizardState.FALLING_BLOCK);
        }
        return null;
    });

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
    }

    @Inject(method = "setLocation(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("RETURN"))
    private void onInit(World world, BlockPos pos, CallbackInfo ci) {
        if (!world.isClient) {
            ((ServerWorld)world).getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(this.getPos()), false).forEach((player) -> {
                Wizard wiz = wizards.get(PolyMapProvider.getPolyMap(player));
                if (wiz != null) wiz.addPlayer(player);
            });
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;pushEntities(F)V"))
    private void onTick(CallbackInfo ci) {
        float d = this.getAmountExtended(this.progress);

        wizards.forEach((polyMap, wizard) -> {
            if (wizard != null) wizard.updatePosition(Vec3d.of(this.getPos()).add(
                    0.5+d*this.facing.getOffsetX(),
                    d*this.facing.getOffsetY(),
                    0.5+d*this.facing.getOffsetZ()));
        });
    }

    @Inject(method = "markRemoved()V", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        wizards.forEach((polyMap, wizard) -> {
            if (wizard != null) wizard.onRemove();
        });
    }

    public MixinPistonBlockEntity(BlockEntityType<?> type) {
        super(type);
    }
}
