package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;

public record StaticFieldValue(String owner, String field) implements StackEntry {
    @Override
    public StackEntry simplify(VirtualMachine vm) throws VmException {
        var clazz = vm.getClass(this.owner());
        vm.ensureClinit(clazz);
        return clazz.getStatic(this.field()).simplify(vm);
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }
}
