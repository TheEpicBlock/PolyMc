package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockStateParticleEffect.class)
public class BlockStateParticleEffectImplementation {
    /**
     * Replaces the {@link net.minecraft.block.BlockState} that {@link net.minecraft.util.collection.IdList#getRawId(Object)} is called with
     */
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;getRawId(Ljava/lang/Object;)I"))
    private BlockState redirectRawId(BlockState in, PacketByteBuf buf) {
        ServerPlayerEntity player = ((PlayerContextContainer)buf).getPolyMcProvidedPlayer();

        if (player == null) {
            throw new NullPointerException("PacketByteBuf did not contain player context");
        }

        return PolyMapProvider.getPolyMap(player).getClientBlock(in);
    }
}
