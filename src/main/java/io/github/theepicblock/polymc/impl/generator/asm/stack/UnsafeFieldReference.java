package io.github.theepicblock.polymc.impl.generator.asm.stack;

/**
 * Returned by jdk.internal.misc.Unsafe#objectFieldOffset(Ljava/lang/Class;Ljava/lang/String;)J
 * It's supposed to return a long, but obviously our JVM doesn't actually keep track of the offsets of fields
 */
public record UnsafeFieldReference(String fieldName) implements StackEntry {
}
