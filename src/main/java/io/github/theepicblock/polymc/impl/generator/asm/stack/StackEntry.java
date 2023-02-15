package io.github.theepicblock.polymc.impl.generator.asm.stack;

import org.apache.commons.lang3.NotImplementedException;

public interface StackEntry {
    static StackEntry knownStackValue(Object o) {
        if (o instanceof Integer i) {
            return new KnownInteger(i);
        }
        if (o instanceof Float f) {
            return new KnownFloat(f);
        }
        return new KnownObject(o);
    }

    default void setField(String name, StackEntry e) {
        throw new NotImplementedException("Can't set field "+name+" on "+this);
    }

    default StackEntry getField(String name) {
        throw new NotImplementedException("Can't get field "+name+" from "+this);
    }
}