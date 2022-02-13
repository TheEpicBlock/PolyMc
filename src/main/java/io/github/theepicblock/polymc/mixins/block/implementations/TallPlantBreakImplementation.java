package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck;
import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see BreakParticleImplementation
 */
@Mixin(TallPlantBlock.class)
public class TallPlantBreakImplementation {
    @Redirect(method = "onBreakInCreative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V"))
    private static void onBreakInCreative(World world, PlayerEntity player, int eventId, BlockPos pos, int data) {
        var spe = (ServerPlayerEntity)player;
        var state = world.getBlockState(pos);

        // Minecraft assumes the player who breaks the block knows it's breaking a block.
        // However, as PolyMc reimplements block breaking server-side, the one breaking the block needs to be notified too
        var needsCustomBreaking = CustomBlockBreakingCheck.needsCustomBreaking(spe, state.getBlock());
        PacketReplacementUtil.syncWorldEvent(world, needsCustomBreaking ? null : player, 2001, pos, state);
    }
}
