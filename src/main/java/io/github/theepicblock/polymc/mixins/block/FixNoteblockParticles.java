package io.github.theepicblock.polymc.mixins.block;

import net.minecraft.block.NoteBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoteBlock.class)
public class FixNoteblockParticles {
    @Redirect(method = "onSyncedBlockEvent(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    public void redirectParticle(World instance, ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (instance instanceof ServerWorld world) {
            world.spawnParticles(parameters, x, y, z, 0, velocityX, velocityY, velocityZ, 1);
        }
    }
}
