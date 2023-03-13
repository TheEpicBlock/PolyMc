package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(EntitySpawnS2CPacket.class)
public class FallingBlockEntityImplementation {
    @Shadow @Final private EntityType<?> entityType;
    @Mutable @Shadow @Final private int entityData;

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private void redirectEntityData(PacketByteBuf buf, CallbackInfo ci) {
        if (this.entityType == EntityType.FALLING_BLOCK) {
            var block = Block.getStateFromRawId(this.entityData);
            this.entityData = Util.getPolydRawIdFromState(block, PacketContext.get().getTarget());
        }
    }
}
