package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;

import java.util.Map;
import java.util.function.Function;

/**
 * @param arbitraryFunction The incoming entry is guaranteed to be concrete. The outcoming entry should be concrete too
 */
public record UnaryArbitraryOp(StackEntry inner, Function<StackEntry, StackEntry> arbitraryFunction) implements StackEntry{
    public boolean canBeSimplified() {
        return (inner.canBeSimplified() || inner.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var entryinner = this.inner;
        if (entryinner.canBeSimplified()) entryinner = entryinner.simplify(vm, simplificationCache);

        if (entryinner.isConcrete()) {
            var result = arbitraryFunction.apply(entryinner);
            assert result.isConcrete();
            return result;
        }
        return new UnaryArbitraryOp(entryinner, arbitraryFunction);
    }
}
