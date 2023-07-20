package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CowCapableMap<T> implements Map<T, @NotNull StackEntry> {
    private CowCapableMap<T> daddy = null;
    private HashMap<T, @NotNull StackEntry> overrides = null;
    private ArrayList<CowCapableMap<T>> children = null;

    public CowCapableMap() {
        this.overrides = new HashMap<>();
    }

    private CowCapableMap(CowCapableMap<T> daddy) {
        this.daddy = daddy;
    }

    public CowCapableMap<T> createClone() {
        var child = new CowCapableMap<>(this);
        if (children == null) children = new ArrayList<>();
        this.children.add(child);
        return child;
    }

    @Override
    public int size() {
        int daddySize = 0;
        if (daddy != null) {
            daddySize = daddy.size();
        }

        int overridesSize = 0;
        if (overrides != null) {
            overridesSize = overrides.size();
        }
        return daddySize + overridesSize;
    }

    @Override
    public boolean isEmpty() {
        boolean daddyEmpty = false;
        if (daddy != null) {
            daddyEmpty = daddy.isEmpty();
        }

        boolean overridesEmpty = false;
        if (overrides != null) {
            overridesEmpty = overrides.isEmpty();
        }
        return daddyEmpty || overridesEmpty;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean daddyContains = false;
        if (daddy != null) {
            daddyContains = daddy.containsKey(key);
        }

        boolean overridesContains = false;
        if (overrides != null) {
            overridesContains = overrides.containsKey(key);
        }
        return daddyContains || overridesContains;
    }

    @Override
    public boolean containsValue(Object value) {
        boolean daddyContains = false;
        if (daddy != null) {
            daddyContains = daddy.containsValue(value);
        }

        boolean overridesContains = false;
        if (overrides != null) {
            overridesContains = overrides.containsValue(value);
        }
        return daddyContains || overridesContains;
    }

    @Override
    public StackEntry get(Object key) {
        if (overrides != null) {
            var o = overrides.get(key);
            if (o != null) return o;
        }
        if (daddy != null) {
            var o = daddy.get(key);
            if (o != null) {
                var copy = o.copy();
                this.put((T)key, copy);
                return copy;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public StackEntry put(T key, StackEntry value) {
        if (overrides == null) overrides = new HashMap<>();
        var previous = overrides.put(key, value);
        if (children != null) {
            for (var child : children) {
                child.put(key, previous); // Preserve the previous value in all the children :3
            }
        }
        return previous;
    }

    @Override
    public StackEntry remove(Object key) {
        throw new NotImplementedException("Don't remove stuff from cows pls");
    }

    @Override
    public void putAll(@NotNull Map<? extends T,? extends StackEntry> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        throw new NotImplementedException("Cows can't be cleared, srry");
    }

    @NotNull
    @Override
    public Set<T> keySet() {
        throw new NotImplementedException("Cows can't be keysetted, srry");
    }

    @NotNull
    @Override
    public Collection<StackEntry> values() {
        throw new NotImplementedException("Cows can't be values, srry");
    }

    @NotNull
    @Override
    public Set<Entry<T,StackEntry>> entrySet() {
        throw new NotImplementedException("Cows can't be entryset, srry");
    }

    public void simplify(VirtualMachine vm) throws MethodExecutor.VmException {
        if (this.overrides != null) {
            for (var entry : overrides.entrySet()) {
                // No need to notify children since a simplification shouldn't change the meaning of the value
                overrides.put(entry.getKey(), entry.getValue().simplify(vm));
            }
        }
        if (this.daddy != null) {
            this.daddy.simplify(vm);
        }
    }
}
