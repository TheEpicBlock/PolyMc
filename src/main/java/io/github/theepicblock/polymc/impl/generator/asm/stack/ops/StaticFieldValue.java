package io.github.theepicblock.polymc.impl.generator.asm.stack.ops;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.InternalName;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public record StaticFieldValue(@InternalName String owner, String field) implements StackEntry {
    @Override
    public boolean canBeSimplified() {
        return true;
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws VmException {
        var clazz = vm.getClass(this.owner());
        vm.ensureClinit(clazz);
        return clazz.getStatic(this.field()).simplify(vm, simplificationCache);
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }
}
