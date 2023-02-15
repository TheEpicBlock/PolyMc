package io.github.theepicblock.polymc.impl.generator.asm.stack;

public record UnknownValue() implements StackEntry {
    public String toString() {
        return "unknown";
    }
}