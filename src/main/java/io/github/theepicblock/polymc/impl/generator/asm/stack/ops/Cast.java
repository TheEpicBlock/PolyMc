package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownFloat;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownLong;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;

public record Cast(StackEntry value, Type in, Type out) implements StackEntry {
    public enum Type {
        INTEGER,
        FLOAT,
        LONG
    }

    @Override
    public JsonElement toJson() {
        return this.tryResolveOnce().toJson();
    }

    @Override
    public StackEntry resolve(VirtualMachine vm) throws VmException {
        return new Cast(value.resolve(vm), in, out).tryResolveOnce();
    }

    public StackEntry tryResolveOnce() {
        Object in;
        try {
            in = this.value.cast((Class<?>)(switch (this.in) {
                case INTEGER -> Integer.class;
                case FLOAT -> Float.class;
                case LONG -> Long.class;
            }));
        } catch (Exception e) {
            return this;
        }

        return switch (this.in) {
            case INTEGER -> switch(this.in) {
                case INTEGER -> new KnownInteger((int)(int)in);
                case FLOAT   -> new KnownFloat((float)(int)in);
                case LONG    ->   new KnownLong((long)(int)in);
            };
            case FLOAT -> switch(this.out) {
                case INTEGER -> new KnownInteger((int)(float)in);
                case FLOAT   -> new KnownFloat((float)(float)in);
                case LONG    ->   new KnownLong((long)(float)in);
            };
            case LONG -> switch(this.in) {
                case INTEGER -> new KnownInteger((int)(long)in);
                case FLOAT   -> new KnownFloat((float)(long)in);
                case LONG    ->   new KnownLong((long)(long)in);
            };
        };
    }
}
