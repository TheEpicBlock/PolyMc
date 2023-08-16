package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;

public record MathOp(StackEntry input, Op op) implements StackEntry {
    public enum Op {
        COS,
        ACOS,
        SIN,
        ASIN,
        TAN,
        ATAN,
        LOG,
        LOG10,
        SQRT
    }
    public boolean canBeSimplified() {
        return (input.canBeSimplified() || input.isConcrete());
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        var inputEntry = this.input;
        if (inputEntry.canBeSimplified()) inputEntry = inputEntry.simplify(vm, simplificationCache);

        if (inputEntry.isConcrete()) {
            var val = inputEntry.extractAs(double.class);
            var result = switch (this.op) {
                case COS -> StrictMath.cos(val);
                case ACOS -> StrictMath.acos(val);
                case SIN -> StrictMath.sin(val);
                case ASIN -> StrictMath.asin(val);
                case TAN -> StrictMath.tan(val);
                case ATAN -> StrictMath.atan(val);
                case LOG -> StrictMath.log(val);
                case LOG10 -> StrictMath.log10(val);
                case SQRT -> StrictMath.sqrt(val);
            };
            return StackEntry.known(result);
        }
        return new MathOp(inputEntry, op);
    }

    @Override
    public void write(PacketByteBuf buf, StackEntryTable table) {
        input.writeWithTag(buf, table);
        buf.writeEnumConstant(op);
    }

    public static StackEntry read(PacketByteBuf buf, StackEntryTable table) {
        return new MathOp(
                StackEntry.readWithTag(buf, table),
                buf.readEnumConstant(Op.class)
        );
    }
}
