package io.github.theepicblock.polymc.mixins.block.implementations.dontforceintcontrol;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.theepicblock.polymc.impl.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PacketCodecs.class)
public interface WriteRegistryValueImplementation {
    @ModifyReturnValue(method = "entryOf", at = @At("RETURN"))
    private static PacketCodec<ByteBuf,BlockState> polyMcWrapCodec(PacketCodec<ByteBuf,BlockState> original, IndexedIterable<BlockState> iterable) {
        if (iterable == Block.STATE_IDS) {
            return original.xmap(
                    state -> state,
                    state -> {
                        var ctx = PacketContext.get();
                        var polymap = Util.tryGetPolyMap(ctx.getClientConnection());
                        return polymap.getClientState(state, ctx.getPlayer());
                    }
            );
        } else {
            return original;
        }
    }
}
