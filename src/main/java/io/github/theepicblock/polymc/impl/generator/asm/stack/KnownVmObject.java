package io.github.theepicblock.polymc.impl.generator.asm.stack;

import java.util.Map;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;

public record KnownVmObject(Clazz type, Map<String, StackEntry> fields) implements StackEntry {
    @Override
    public StackEntry getField(String name) {
        return this.fields().getOrDefault(name, new UnknownValue());
    }

    @Override
    public void setField(String name, StackEntry e) {
        this.fields().put(name, e);
    }
}
