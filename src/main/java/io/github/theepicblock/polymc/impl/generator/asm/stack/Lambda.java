package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.objectweb.asm.Handle;

public record Lambda(Handle method) implements StackEntry {
}