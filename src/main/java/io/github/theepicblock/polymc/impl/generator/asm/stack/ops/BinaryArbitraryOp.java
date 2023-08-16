package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

import java.util.function.BiFunction;

/**
 * @param arbitraryFunction The incoming entry is guaranteed to be concrete. The outcoming entry should be concrete too
 */
public record BinaryArbitraryOp(StackEntry inner, StackEntry inner2, BiFunction<StackEntry, StackEntry, StackEntry> arbitraryFunction) implements StackEntry{
    public boolean canBeSimplified() {
        return (inner.canBeSimplified() || inner.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var entryinner = this.inner;
        if (entryinner.canBeSimplified()) entryinner = entryinner.simplify(vm, simplificationCache);
        var entryinner2 = this.inner2;
        if (entryinner2.canBeSimplified()) entryinner2 = entryinner2.simplify(vm, simplificationCache);

        if (entryinner.isConcrete() && entryinner2.isConcrete()) {
            var result = arbitraryFunction.apply(entryinner, entryinner2);
            assert result.isConcrete();
            return result;
        }
        return new BinaryArbitraryOp(entryinner, entryinner2, arbitraryFunction);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        inner.writeWithTag(buf, table);
        inner2.writeWithTag(buf, table);
        // TODO
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new BinaryArbitraryOp(StackEntry.readWithTag(buf, table), StackEntry.readWithTag(buf, table), null);
    }
}
