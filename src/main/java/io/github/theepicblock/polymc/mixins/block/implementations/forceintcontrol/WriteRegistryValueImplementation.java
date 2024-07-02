package io.github.theepicblock.polymc.mixins.block.implementations.forceintcontrol;

import io.github.theepicblock.polymc.impl.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

import static net.minecraft.network.codec.PacketCodecs.indexed;

@Mixin(PacketCodecs.class)
public interface WriteRegistryValueImplementation {
    /**
     * @author TheEpicBlock
     * @reason There's only one int value that's actually written. And only one mod that can choose it.
     *         A more compatible mixin is used if you don't enable the `forceBlockIdIntControl` config option
     */
    @Overwrite
    static <T> PacketCodec<ByteBuf,T> entryOf(IndexedIterable<T> iterable) {
        Objects.requireNonNull(iterable);
        if (iterable == Block.STATE_IDS) {
            return indexed(iterable::getOrThrow, state -> {
                var ctx = PacketContext.get();
                var polymap = Util.tryGetPolyMap(ctx.getClientConnection());
                return polymap.getClientStateRawId((BlockState)state, ctx.getPlayer());
            });
        } else {
            return indexed(iterable::getOrThrow, iterable::getRawIdOrThrow);
        }
    }
}
