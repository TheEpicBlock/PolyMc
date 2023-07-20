package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;

import java.util.Map;

public record ArrayLength(StackEntry array) implements StackEntry {
    @Override
    public boolean canBeSimplified() {
        return array.canBeSimplified() || array.isConcrete();
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var array = this.array;
        if (array.canBeSimplified()) array = array.simplify(vm, simplificationCache);

        if (array.isConcrete()) {
            return new KnownInteger(array.extractAs(Object[].class).length);
        } else {
            return new ArrayLength(array);
        }
    }

    @Override
    public JsonElement toJson() {
        return null;
    }
}
