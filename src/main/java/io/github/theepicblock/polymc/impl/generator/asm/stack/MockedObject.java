package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.AsmUtils;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

public record MockedObject(@NotNull Origin origin, @Nullable VirtualMachine.Clazz type, CowCapableMap<String> overrides) implements StackEntry {
    public MockedObject(@NotNull Origin origin, @Nullable VirtualMachine.Clazz type) {
        this(origin, type, new CowCapableMap<>());
    }

    @Override
    public void setField(String name, StackEntry e) throws MethodExecutor.VmException {
        this.overrides.put(name, e);
    }

    @Override
    public @NotNull StackEntry getField(String name) throws MethodExecutor.VmException {
        var o = overrides.get(name);
        if (o != null) return o;
        var field = AsmUtils.getFields(type)
                .filter(f -> f.name.equals(name))
                .filter(f -> !AsmUtils.hasFlag(f, Opcodes.ACC_STATIC))
                .findAny().orElse(null);
        if (field != null && type != null) {
            var vm = type.getLoader();
            var type = switch (field.desc) {
                case "I", "Z", "S", "C", "B" -> vm.getClass("java/lang/Integer");
                case "J" -> vm.getClass("java/lang/Long");
                case "F" -> vm.getClass("java/lang/Float");
                case "D" -> vm.getClass("java/lang/Double");
                default -> {
                    if (field.desc.startsWith("L") && field.desc.endsWith(";")) {
                        yield vm.getClass(field.desc.substring(1, field.desc.length()-1));
                    }
                    yield null;
                }
            };
            return new MockedObject(new FieldAccess(this, name), type, new CowCapableMap<>());
        } else {
            return new MockedObject(new FieldAccess(this, name), null, new CowCapableMap<>());
        }
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        throw new NotImplementedException("Can't cast a mocked object of type "+this.type+" to "+type);
    }

    public interface Origin {

    }

    public record Root() implements Origin {

    }

    public record FieldAccess(StackEntry root, String fieldName) implements Origin {

    }
}
