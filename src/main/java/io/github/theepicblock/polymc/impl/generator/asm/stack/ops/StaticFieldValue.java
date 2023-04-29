package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;

public record StaticFieldValue(String owner, String field) implements StackEntry {
    @Override
    public StackEntry resolve(VirtualMachine vm) throws VmException {
        var clazz = vm.getClass(this.owner());
        vm.ensureClinit(clazz);
        return clazz.getStatic(this.field()).resolve(vm);
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }
}
