package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.Objects;

public record MethodCall(VirtualMachine.Clazz currentClass, @NotNull MethodInsnNode inst, @NotNull StackEntry[] arguments) implements StackEntry {
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
        return Objects.equals(currentClass, that.currentClass) && instEquals(inst, that.inst) && Arrays.deepEquals(arguments, that.arguments);
    }

    private static boolean instEquals(@NotNull MethodInsnNode a, @NotNull MethodInsnNode b) {
        if (a == b) return true;
        return a.getOpcode() == b.getOpcode() && Objects.equals(a.name, b.name) && Objects.equals(a.owner, b.owner) && Objects.equals(a.desc, b.desc);
     }

    @Override
    public int hashCode() {
        var instHash = Objects.hash(inst.owner, inst.name, inst.desc, inst.getOpcode());
        int result = Objects.hash(currentClass, instHash);
        result = 31 * result + Arrays.deepHashCode(arguments);
        return result;
    }
}
