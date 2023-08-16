package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import net.minecraft.network.PacketByteBuf;

/**
 * Returned by jdk.internal.misc.Unsafe#objectFieldOffset(Ljava/lang/Class;Ljava/lang/String;)J
 * It's supposed to return a long, but obviously our JVM doesn't actually keep track of the offsets of fields
 */
public record UnsafeFieldReference(String fieldName) implements StackEntry {

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeString(fieldName);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new UnsafeFieldReference(buf.readString());
    }
}
