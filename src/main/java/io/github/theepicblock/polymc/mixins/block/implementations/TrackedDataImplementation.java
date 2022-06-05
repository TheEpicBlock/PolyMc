package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$18")
public class TrackedDataImplementation {
    @Redirect(method = "write(Lnet/minecraft/network/PacketByteBuf;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private int redirectGetRawId(BlockState state, PacketByteBuf packetByteBuf, Optional<BlockState> blockState) {
        return Util.getPolydRawIdFromState(state, PacketContext.get().getTarget());
    }
}
