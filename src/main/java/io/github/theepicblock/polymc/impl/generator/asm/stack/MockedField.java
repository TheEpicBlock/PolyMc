package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents the result of any field access. As a result, it can represent any type.
 */
public record MockedField(StackEntry origin, String field, HashMap<String, StackEntry> setFields) implements StackEntry {
    public MockedField(StackEntry origin, String field) {
        this(origin, field, new HashMap<>());
    }

    @Override
    public void setField(String name, StackEntry e) {
        this.setFields.put(name, e);
    }

    @Override
    public @NotNull StackEntry getField(String name) throws MethodExecutor.VmException {
        if (this.setFields.containsKey(name)) {
            return setFields.get(name);
        } else {
            return new MockedField(this, name);
        }
    }

    @Override
    public @NotNull StackEntry arrayAccess(int index) throws MethodExecutor.VmException {
        throw new NotImplementedException();
    }

    @Override
    public void arraySet(int index, @NotNull StackEntry entry) throws MethodExecutor.VmException {
        throw new NotImplementedException();
    }

    @Override
    public JsonElement toJson() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "MockedField{" +
                "field='" + field + '\'' +
                ", setFields=" + setFields +
                '}';
    }
}
