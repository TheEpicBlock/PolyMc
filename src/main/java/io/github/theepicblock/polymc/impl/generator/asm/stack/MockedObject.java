package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.generator.asm.AsmUtils;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

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
            var type = vm.getType(Type.getType(field.desc));
            return new MockedObject(new FieldAccess(this, name), type, new CowCapableMap<>());
        } else {
            return new MockedObject(new FieldAccess(this, name), null, new CowCapableMap<>());
        }
    }

    @Override
    public int getWidth() {
        if (type != null && ("java/lang/Double".equals(type.name()) || "java/lang/Long".equals(type.name()))) {
            return 2;
        }
        return StackEntry.super.getWidth();
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
