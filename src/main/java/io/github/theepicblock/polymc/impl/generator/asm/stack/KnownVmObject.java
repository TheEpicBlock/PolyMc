package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record KnownVmObject(@NotNull Clazz type, @NotNull Map<@NotNull String, @NotNull StackEntry> fields) implements StackEntry {
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
    public StackEntry simplify(VirtualMachine vm) throws VmException {
        for (var entry : fields.entrySet()) {
            fields.put(entry.getKey(), entry.getValue().simplify(vm));
        }
        return this;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public StackEntry copy() {
        var newMap = new HashMap<String, StackEntry>();
        this.fields.forEach((key, val) -> {
            newMap.put(key, val.copy());
        });
        return new KnownVmObject(this.type, newMap);
    }
}
