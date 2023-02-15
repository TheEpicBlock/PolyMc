package io.github.theepicblock.polymc.impl.generator.asm.stack;

public interface StackEntry {

    static StackEntry knownStackValue(Object o) {
        if (o instanceof Integer i) {
            return new KnownInteger(i);
        }
        return new KnownObject(o);
    }

}