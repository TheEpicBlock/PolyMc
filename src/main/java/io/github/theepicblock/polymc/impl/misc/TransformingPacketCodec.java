package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.function.BiFunction;

public record TransformingPacketCodec<B, V>(PacketCodec<B, V> codec, BiFunction<B, V, V> encodeTransform, BiFunction<B, V, V> decodeTransform) implements PacketCodec<B, V> {
    private static final BiFunction<PacketByteBuf, Object, Object> PASSTHROUGH = (b, o) -> o;

    @Override
    public V decode(B buf) {
        return decodeTransform.apply(buf, this.codec.decode(buf));
    }

    @Override
    public void encode(B buf, V value) {
        this.codec.encode(buf, this.encodeTransform.apply(buf, value));
    }

    public static <B, V> PacketCodec<B, V> encodeOnly(PacketCodec<B, V> codec, BiFunction<B, V, V> encodeTransform) {
        //noinspection unchecked
        return new TransformingPacketCodec<>(codec, encodeTransform, (BiFunction<B, V, V>) PASSTHROUGH);
    }
}