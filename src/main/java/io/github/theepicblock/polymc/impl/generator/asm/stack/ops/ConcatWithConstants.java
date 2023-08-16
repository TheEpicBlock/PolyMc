package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

public record ConcatWithConstants(StackEntry toInsert, String constant) implements StackEntry {
    public boolean canBeSimplified() {
        return (toInsert.canBeSimplified() || toInsert.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var entryinner = this.toInsert;
        if (entryinner.canBeSimplified()) entryinner = entryinner.simplify(vm, simplificationCache);

        if (entryinner.isConcrete()) {
            var result = constant.replace("\u0001", entryinner.extractAs(String.class));
            return StackEntry.known(result);
        }
        return new ConcatWithConstants(entryinner, constant);
    }
    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeString(constant);
        toInsert.writeWithTag(buf, table);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        var constant = buf.readString();
        var toInsert = StackEntry.readWithTag(buf, table);
        return new ConcatWithConstants(toInsert, constant);
    }
}
