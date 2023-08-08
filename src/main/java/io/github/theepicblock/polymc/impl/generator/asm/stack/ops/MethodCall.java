package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.objectweb.asm.tree.MethodInsnNode;

public record MethodCall(VirtualMachine.Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) implements StackEntry {
    @Override
    public int getWidth() {
        if (inst.desc.endsWith("J") || inst.desc.endsWith("D")) {
            return 2;
        }
        return StackEntry.super.getWidth();
    }
}
