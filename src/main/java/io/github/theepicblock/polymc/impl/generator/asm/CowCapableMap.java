package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class CowCapableMap<T> {
    private @Nullable CowCapableMap<T> daddy = null;
    private @Nullable HashMap<T, StackEntry> overrides = null;
    private @Nullable ArrayList<CowCapableMap<T>> children = null;

    public CowCapableMap() {
        this.overrides = new HashMap<>();
    }

    private CowCapableMap(@NotNull CowCapableMap<T> daddy) {
        this.daddy = daddy;
    }

    public CowCapableMap<T> createClone() {
        var child = new CowCapableMap<>(this);
        if (children == null) children = new ArrayList<>();
        this.children.add(child);
        return child;
    }

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

    public boolean containsKey(T key) {
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

    public boolean containsValue(StackEntry value) {
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

    public StackEntry get(T key) {
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
    public StackEntry put(T key, StackEntry value) {
        if (overrides == null) overrides = new HashMap<>();
        var previous = overrides.put(key, value);
        if (children != null) {
            for (var child : children) {
                if (child.overrides == null) child.overrides = new HashMap<>();
                if (!child.overrides.containsKey(key)) {
                    child.overrides.put(key, previous); // Preserve the previous value in all the children :3
                }
            }
        }
        return previous;
    }

    public StackEntry remove(Object key) {
        throw new NotImplementedException("Don't remove stuff from cows pls");
    }

    public void putAll(@NotNull Map<? extends T,? extends StackEntry> m) {
        m.forEach(this::put);
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

    public void forEach(BiConsumer<? super T,? super StackEntry> action) {
        if (this.overrides != null) {
            this.overrides.forEach(action);
            if (this.daddy != null) {
                this.daddy.forEach((key, val) -> {
                    if (!overrides.containsKey(key)) {
                        action.accept(key, val);
                    }
                });
            }
        } else if (this.daddy != null) {
            this.daddy.forEach(action);
        }
    }
}
