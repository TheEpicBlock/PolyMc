package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Returned by jdk.internal.misc.Unsafe#objectFieldOffset(Ljava/lang/Class;Ljava/lang/String;)J
 * It's supposed to return a long, but obviously our JVM doesn't actually keep track of the offsets of fields
 */
public record UnsafeFieldReference(String fieldName) implements StackEntry {
    @Override
    public JsonElement toJson() {
        throw new NotImplementedException("Can't convert UnsafeFieldReference to json");
    }
}
