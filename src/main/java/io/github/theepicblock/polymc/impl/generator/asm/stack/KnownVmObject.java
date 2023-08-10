package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.impl.generator.asm.AsmUtils;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public record KnownVmObject(@NotNull Clazz type, @NotNull CowCapableMap<@NotNull String> fields) implements StackEntry {
    public KnownVmObject(@NotNull Clazz type) {
        this(type, new CowCapableMap<>());
    }

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
            var result = switch (field.desc) {
                case "I", "Z", "S", "C", "B" -> new KnownInteger(0);
                case "J" -> new KnownLong(0);
                case "F" -> new KnownFloat(0);
                case "D" -> new KnownDouble(0);
                default -> KnownObject.NULL;
            };
            this.fields.put(name, result);
            return result;
        }
        return value;
    }

    @Override
    public void setField(String name, @NotNull StackEntry e) {
        this.fields().put(name, e);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if ((type == int.class && this.type.getNode().name.equals("java/lang/Integer")) ||
                (type == long.class && this.type.getNode().name.equals("java/lang/Long")) ||
                (type == float.class && this.type.getNode().name.equals("java/lang/Float")) ||
                (type == double.class && this.type.getNode().name.equals("java/lang/Double"))) {
            return this.getField("value").extractAs(type);
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public JsonElement toJson() {
        var element = new JsonObject();
        this.fields.forEachImmutable((key, val) -> {
            element.add(key, val.toJson());
        });
        return element;
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws VmException {
        if (simplificationCache.containsKey(this)) return simplificationCache.get(this);
        simplificationCache.put(this, this);
//        this.fields.simplify(vm, simplificationCache);
        return this;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public StackEntry copy(Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        if (copyCache.containsKey(this)) return copyCache.get(this);
        var newMap = new CowCapableMap<String>();
        var newObj = new KnownVmObject(this.type, newMap);
        copyCache.put(this, newObj);
        newMap.clearAndCopy(this.fields, copyCache);
        return newObj;
    }

    // We're overriding these because the type shouldn't really matter
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnownVmObject that = (KnownVmObject)o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "KnownVmObject["+type+"]";
    }
}
