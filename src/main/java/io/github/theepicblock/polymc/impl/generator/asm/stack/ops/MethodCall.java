package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.Objects;

public record MethodCall(VirtualMachine.Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) implements StackEntry {
    @Override
    public int getWidth() {
        if (inst.desc.endsWith("J") || inst.desc.endsWith("D")) {
            return 2;
        }
        return StackEntry.super.getWidth();
    }

    @Override
    public String toString() {
        return "MethodCall["+inst.owner+"#"+inst.name+"]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCall that = (MethodCall)o;
        return Objects.equals(currentClass, that.currentClass) && Objects.equals(inst, that.inst) && Arrays.deepEquals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(currentClass, inst);
        result = 31 * result + Arrays.deepHashCode(arguments);
        return result;
    }
}
