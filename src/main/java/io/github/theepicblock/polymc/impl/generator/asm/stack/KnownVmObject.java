package io.github.theepicblock.polymc.impl.generator.asm.stack;

import java.util.Map;

import com.google.gson.JsonElement;

import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;

public record KnownVmObject(Clazz type, Map<String, StackEntry> fields) implements StackEntry {
    @Override
    public StackEntry getField(String name) {
        return this.fields().getOrDefault(name, new UnknownValue("Don't know value of field"));
    }

    @Override
    public void setField(String name, StackEntry e) {
        this.fields().put(name, e);
    }

    @Override
    public JsonElement toJson() {
        return StackEntry.GSON.toJsonTree(fields);
    }

    @Override
    public StackEntry resolve(VirtualMachine vm) throws VmException {
        for (var entry : fields.entrySet()) {
            fields.put(entry.getKey(), entry.getValue().resolve(vm));
        }
        return this;
    }
}
