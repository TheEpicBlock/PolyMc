package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownArray;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

public record NewArray(StackEntry length) implements StackEntry {
    public boolean canBeSimplified() {
        return (length.canBeSimplified() || length.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var lengthEntry = this.length;
        if (lengthEntry.canBeSimplified()) lengthEntry = lengthEntry.simplify(vm, simplificationCache);

        if (lengthEntry.isConcrete()) {
            return KnownArray.withLength(lengthEntry.extractAs(int.class));
        }
        return new NewArray(lengthEntry);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        length.writeWithTag(buf, table);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new NewArray(StackEntry.readWithTag(buf, table));
    }
}
