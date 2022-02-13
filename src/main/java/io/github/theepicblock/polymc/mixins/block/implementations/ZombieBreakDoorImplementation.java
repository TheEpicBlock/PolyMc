package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.mixin.PacketReplacementUtil;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BreakDoorGoal.class)
public abstract class ZombieBreakDoorImplementation extends DoorInteractGoal {
    public ZombieBreakDoorImplementation(MobEntity mob) {
        super(mob);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V", ordinal = 1))
    private void redirectWorldEvent(World world, int eventId, BlockPos pos, int data) {
        PacketReplacementUtil.syncWorldEvent(world, null, eventId, pos, this.mob.world.getBlockState(this.doorPos));
    }
}
