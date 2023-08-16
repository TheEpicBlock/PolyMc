package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.InternalName;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

/**
 * @param owner should be the *actual* owner of the static field. This code won't deal with inheritance
 */
public record StaticFieldValue(@InternalName String owner, String field) implements StackEntry {
    @Override
    public boolean canBeSimplified() {
        return true;
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws VmException {
        var clazz = vm.getClass(this.owner());
        vm.ensureClinit(clazz);
        return clazz.getStatic(this.field()).simplify(vm, simplificationCache);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        buf.writeString(owner);
        buf.writeString(field);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new StaticFieldValue(buf.readString(), buf.readString());
    }
}
