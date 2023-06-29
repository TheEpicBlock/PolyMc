package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownFloat;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownLong;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;

public record Cast(StackEntry value, Type in, Type out) implements StackEntry {
    public enum Type {
        INTEGER,
        FLOAT,
        LONG
    }

    @Override
    public boolean canBeSimplified() {
        return value.canBeSimplified() || value.isConcrete();
    }

    @Override
    public StackEntry simplify(VirtualMachine vm) throws VmException {
        var value = this.value;
        if (value.canBeSimplified()) {
            value = value.simplify(vm);
        }

        if (value.isConcrete()) {
            Object in = this.value.extractAs((Class<?>)(switch (this.in) {
                case INTEGER -> Integer.class;
                case FLOAT -> Float.class;
                case LONG -> Long.class;
            }));

            return switch (this.in) {
                case INTEGER -> switch(this.out) {
                    case INTEGER -> new KnownInteger((int)(int)in);
                    case FLOAT   -> new KnownFloat((float)(int)in);
                    case LONG    ->   new KnownLong((long)(int)in);
                };
                case FLOAT -> switch(this.out) {
                    case INTEGER -> new KnownInteger((int)(float)in);
                    case FLOAT   -> new KnownFloat((float)(float)in);
                    case LONG    ->   new KnownLong((long)(float)in);
                };
                case LONG -> switch(this.out) {
                    case INTEGER -> new KnownInteger((int)(long)in);
                    case FLOAT   -> new KnownFloat((float)(long)in);
                    case LONG    ->   new KnownLong((long)(long)in);
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
