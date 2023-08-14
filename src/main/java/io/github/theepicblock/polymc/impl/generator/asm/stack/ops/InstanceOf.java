package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.InternalName;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

public record InstanceOf(StackEntry entry, @InternalName String toCheck) implements StackEntry {
    @Override
    public boolean canBeSimplified() {
        return entry.canBeSimplified() || entry.isConcrete();
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws VmException {
        StackEntry entry1 = entry;
        if (entry1.canBeSimplified()) entry1 = entry1.simplify(vm);

        if (entry1 instanceof KnownObject o && o.i() == null) {
            return StackEntry.known(false);
        }
        var type = vm.getType(entry1);
        if (type == null) {
            return new InstanceOf(entry1, toCheck);
        } else {
            return StackEntry.known(type.name().equals(toCheck));
        }
    }

    public int toInt() throws VmException {
        return toInt(this.entry, this.toCheck);
    }

    public static int toInt(StackEntry entry, String toCheck) throws VmException {
        if (entry instanceof KnownVmObject o) {
            return o.type().getNode().name.equals(toCheck) ? 1 : 0;
        } else if (entry instanceof KnownObject o) {
            if (o.i() == null) {
                return 0;
            } else {
                return o.i().getClass().getName().equals(toCheck) ? 1 : 0;
            }
        }
        throw new VmException("Can't find instance of "+entry, null);
    }

    @Override
    public void write(PacketByteBuf buf) {
        entry.writeWithTag(buf);
        buf.writeString(toCheck);
    }

    public static StackEntry read(PacketByteBuf buf) {
        return new InstanceOf(StackEntry.readWithTag(buf), buf.readString());
    }
}
