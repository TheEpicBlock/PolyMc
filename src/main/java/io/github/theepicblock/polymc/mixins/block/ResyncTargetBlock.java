package io.github.theepicblock.polymc.mixins.block;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.BlockResyncManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.TargetBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TargetBlock.class)
public class ResyncTargetBlock {
    @Inject(method = "setPower(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/block/BlockState;ILnet/minecraft/util/math/BlockPos;I)V", at = @At("HEAD"))
    private static void onSetPower(WorldAccess world, BlockState state, int power, BlockPos pos, int delay, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(pos)).forEach(player -> {
                if (Util.tryGetPolyMap(player).isVanillaLikeMap()) {
                    BlockResyncManager.onBlockUpdate(null, pos, serverWorld, player, null);
                }
            });
        }
    }
}
