package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

public record RawBitOp(StackEntry input, Op op) implements StackEntry {
    public enum Op {
        Float2Int,
        Int2Float,
        Double2Long,
        Long2Double
    }
    public boolean canBeSimplified() {
        return (input.canBeSimplified() || input.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var inputEntry = this.input;
        if (inputEntry.canBeSimplified()) inputEntry = inputEntry.simplify(vm, simplificationCache);

        if (inputEntry.isConcrete()) {
            return switch (this.op) {
                case Float2Int -> StackEntry.known(Float.floatToRawIntBits(inputEntry.extractAs(float.class)));
                case Int2Float -> StackEntry.known(Float.intBitsToFloat(inputEntry.extractAs(int.class)));
                case Double2Long -> StackEntry.known(Double.doubleToRawLongBits(inputEntry.extractAs(double.class)));
                case Long2Double -> StackEntry.known(Double.longBitsToDouble(inputEntry.extractAs(long.class)));
            };
        }
        return new RawBitOp(inputEntry, op);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        input.writeWithTag(buf, table);
        buf.writeEnumConstant(op);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new RawBitOp(
                StackEntry.readWithTag(buf, table),
                buf.readEnumConstant(RawBitOp.Op.class)
        );
    }
}
