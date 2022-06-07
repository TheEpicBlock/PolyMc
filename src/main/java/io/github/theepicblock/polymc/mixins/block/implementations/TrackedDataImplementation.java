package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$2")
public class TrackedDataImplementation {
    @ModifyArg(method = "write(Lnet/minecraft/network/PacketByteBuf;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState redirectGetRawId(BlockState state) {
        var player = PacketContext.get().getTarget();
        var map = Util.tryGetPolyMap(player);

        return map.getClientState(state, player);
    }
}
