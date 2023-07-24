package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public record Cast(StackEntry value, Type in, Type out) implements StackEntry {
    public enum Type {
        INTEGER,
        FLOAT,
        LONG,
        DOUBLE
    }

    @Override
    public boolean canBeSimplified() {
        return value.canBeSimplified() || value.isConcrete();
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws VmException {
        var value = this.value;
        if (value.canBeSimplified()) {
            value = value.simplify(vm, simplificationCache);
        }

        if (value.isConcrete()) {
            Object in = value.extractAs((Class<?>)(switch (this.in) {
                case INTEGER -> Integer.class;
                case FLOAT -> Float.class;
                case LONG -> Long.class;
                case DOUBLE -> Double.class;
            }));

            return switch (this.in) {
                case INTEGER -> switch(this.out) {
                    case INTEGER ->   new KnownInteger((int)(int)in);
                    case FLOAT   ->   new KnownFloat((float)(int)in);
                    case LONG    ->     new KnownLong((long)(int)in);
                    case DOUBLE  -> new KnownDouble((double)(int)in);
                };
                case FLOAT -> switch(this.out) {
                    case INTEGER ->   new KnownInteger((int)(float)in);
                    case FLOAT   ->   new KnownFloat((float)(float)in);
                    case LONG    ->     new KnownLong((long)(float)in);
                    case DOUBLE  -> new KnownDouble((double)(float)in);
                };
                case LONG -> switch(this.out) {
                    case INTEGER ->   new KnownInteger((int)(long)in);
                    case FLOAT   ->   new KnownFloat((float)(long)in);
                    case LONG    ->     new KnownLong((long)(long)in);
                    case DOUBLE  -> new KnownDouble((double)(long)in);
                };
                case DOUBLE -> switch(this.out) {
                    case INTEGER ->   new KnownInteger((int)(double)in);
                    case FLOAT   ->   new KnownFloat((float)(double)in);
                    case LONG    ->     new KnownLong((long)(double)in);
                    case DOUBLE  -> new KnownDouble((double)(double)in);
                };
            };
        } else {
            return new Cast(value, in, out);
        }
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }
}
