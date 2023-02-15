package io.github.theepicblock.polymc.impl.generator.asm.stack;

public record KnownVoid() implements StackEntry {
    public String toString() {
        return "void";
    }

}