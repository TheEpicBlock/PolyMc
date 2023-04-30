package io.github.theepicblock.polymc.impl.generator.asm.stack;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;

public record KnownVmObject(@NotNull Clazz type, @NotNull Map<String, StackEntry> fields) implements StackEntry {
    @Override
    public @NotNull StackEntry getField(String name) {
        var value = this.fields().get(name);
        if (value == null) {
            // We need to get the default value depending on the type of the field
            var field = type.getNode().fields.stream().filter(f -> f.name.equals(name)).findAny().orElse(null);
            if (field == null) {
                return new UnknownValue("Don't know value of field '"+name+"'");
            }
            return switch (field.desc) {
                case "I" -> new KnownInteger(0);
                case "J" -> new KnownLong(0);
                case "F" -> new KnownFloat(0);
                default -> KnownObject.NULL;
            };
        }
        return value;
    }

    @Override
    public void setField(String name, StackEntry e) {
        if (e == this) {
            return; // I just… don't want to deal with that
        }
        this.fields().put(name, e);
    }

    @Override
    public JsonElement toJson() {
        var element = new JsonObject();
        for (var f : this.fields.entrySet()) {
            element.add(f.getKey(), f.getValue().toJson());
        }
        return element;
    }

    @Override
    public StackEntry resolve(VirtualMachine vm) throws VmException {
        for (var entry : fields.entrySet()) {
            fields.put(entry.getKey(), entry.getValue().resolve(vm));
        }
        return this;
    }
}
