package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockStateParticleEffect.class)
public class BlockStateParticleEffectImplementation<T> {
    /**
     * Replaces the {@link net.minecraft.block.BlockState} that {@link net.minecraft.util.collection.IdList#getRawId(Object)} is called with
     */
    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;getRawId(Ljava/lang/Object;)I"))
    private int redirectRawId(IdList<T> idList, T in, PacketByteBuf buf) {
        ServerPlayerEntity player = PlayerContextContainer.retrieve(buf);

        if (player == null) {
            throw new NullPointerException("PacketByteBuf did not contain player context");
        }

        return Util.getPolydRawIdFromState((BlockState)in, player);
    }
}
