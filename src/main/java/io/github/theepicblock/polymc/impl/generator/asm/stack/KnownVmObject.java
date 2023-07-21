package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.impl.generator.asm.AsmUtils;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public record KnownVmObject(@NotNull Clazz type, @NotNull CowCapableMap<@NotNull String> fields) implements StackEntry {
    @Override
    public @NotNull StackEntry getField(String name) {
        var value = this.fields().get(name);
        if (value == null) {
            // We need to get the default value depending on the type of the field
            var field = AsmUtils.getFields(type)
                    .filter(f -> f.name.equals(name))
                    .filter(f -> !AsmUtils.hasFlag(f, Opcodes.ACC_STATIC))
                    .findAny().orElse(null);
            if (field == null) {
                return new UnknownValue("Don't know value of field '"+name+"'");
            }
            return switch (field.desc) {
                case "I", "Z", "S", "C", "B" -> new KnownInteger(0);
                case "J" -> new KnownLong(0);
                case "F" -> new KnownFloat(0);
                case "D" -> new KnownDouble(0);
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
        this.fields.forEach((key, val) -> {
            element.add(key, val.toJson());
        });
        return element;
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Map<StackEntry,StackEntry> simplificationCache) throws VmException {
        if (simplificationCache.containsKey(this)) return simplificationCache.get(this);
        simplificationCache.put(this, this);

        this.fields.simplify(vm, simplificationCache);
        return this;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public StackEntry copy() {
        return new KnownVmObject(this.type, this.fields.createClone());
    }
}
