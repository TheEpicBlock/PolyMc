package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class CowCapableMap<T> {
    private static final ReferenceQueue<CowCapableMap<?>> GLOBAL_CLEANING_MAP = new ReferenceQueue<>();
    private @Nullable CowCapableMap<T> daddy = null;
    private @Nullable HashMap<T, @Nullable StackEntry> overrides = null;
    private @Nullable ArrayList<CowWeakReference<T>> children = null;

    public CowCapableMap() {
        this.overrides = new HashMap<>();
    }

    private CowCapableMap(@NotNull CowCapableMap<T> daddy) {
        this.daddy = daddy;
    }

    public CowCapableMap<T> createClone() {
        var child = new CowCapableMap<>(this);
        if (children == null) children = new ArrayList<>();
        this.children.add(new CowWeakReference<>(child, GLOBAL_CLEANING_MAP));
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
            overridesContains = overrides.get(key) != null;
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
            if (o != null) {
                iterChildren(child -> {
                    if (child.overrides == null) child.overrides = new HashMap<>();
                    if (!child.overrides.containsKey(key)) {
                        child.overrides.put(key, o.copy()); // Just in case this one will be edited
                    }
                });
                return o;
            }
        }
        if (daddy != null) {
            var o = daddy.get(key);
            if (o != null) {
                var copy = o.copy();
                if (this.overrides == null) this.overrides = new HashMap<>();
                this.overrides.put(key, copy);
                iterChildren(child -> {
                    if (child.overrides == null) child.overrides = new HashMap<>();
                    if (!child.overrides.containsKey(key)) {
                        child.overrides.put(key, o.copy()); // Just in case this one will be edited
                    }
                });
                return copy;
            }
        }
        return null;
    }

    public StackEntry getErased(Object key) {
        return get((T)key);
    }

    private StackEntry getImmutable(T key) {
        if (overrides != null) {
            var o = overrides.get(key);
            if (o != null) {
                return o;
            };
        }
        if (daddy != null) {
            return daddy.getImmutable(key);
        }
        return null;
    }

    private StackEntry getImmutableErased(Object key) {
        return get((T)key);
    }

    @Nullable
    public StackEntry put(T key, @NotNull StackEntry value) {
        if (overrides == null) overrides = new HashMap<>();
        var previous = overrides.put(key, value);
        iterChildren(child -> {
            if (child.overrides == null) child.overrides = new HashMap<>();
            if (!child.overrides.containsKey(key)) {
                child.overrides.put(key, previous); // Preserve the previous value in all the children :3
            }
        });
        return previous;
    }

    private void iterChildren(Consumer<CowCapableMap<T>> childConsumer) {
        checkGlobalCleaningQueue();
        if (children != null) {
            var childIter = children.iterator();
            while (childIter.hasNext()) {
                var child = childIter.next().get();
                if (child == null) {
                    childIter.remove();
                    continue;
                }

                childConsumer.accept(child);
            }
        }
    }

    public StackEntry remove(Object key) {
        throw new NotImplementedException("Don't remove stuff from cows pls");
    }

    public void putAll(@NotNull Map<? extends T,? extends StackEntry> m) {
        m.forEach(this::put);
    }

    public void simplify(VirtualMachine vm, Map<StackEntry, StackEntry> simplificationCache) throws MethodExecutor.VmException {
        if (this.overrides != null) {
            for (var entry : overrides.entrySet()) {
                if (entry.getValue() == null) continue;
                // No need to notify children since a simplification shouldn't change the meaning of the value
                overrides.put(entry.getKey(), entry.getValue().simplify(vm, simplificationCache));
            }
        }
        if (this.daddy != null) {
            this.daddy.simplify(vm, simplificationCache);
        }
    }

    public void forEachImmutable(BiConsumer<? super T,? super StackEntry> action) {
        if (this.overrides != null) {
            this.overrides.forEach((key, val) -> {
                if (val != null) {
                    // A null value indicates a field isn't supposed to exist
                    action.accept(key, val);
                }
            });
            if (this.daddy != null) {
                this.daddy.forEachImmutable((key, val) -> {
                    if (!overrides.containsKey(key)) {
                        action.accept(key, val);
                    }
                });
            }
        } else if (this.daddy != null) {
            this.daddy.forEachImmutable(action);
        }
    }

    public boolean findAny(ToBooleanBiFunction<? super T,? super StackEntry> filter) {
        if (this.overrides != null) {
            for (var entry : this.overrides.entrySet()) {
                if (filter.applyAsBoolean(entry.getKey(), entry.getValue())) {
                    return true;
                }
            }
            if (this.daddy != null) {
                return this.daddy.findAny((key, val) -> !overrides.containsKey(key) && filter.applyAsBoolean(key, val));
            }
        } else if (this.daddy != null) {
            return this.daddy.findAny(filter);
        }
        return false;
    }

    /**
     * 5000iq, very good programming
     */
    private boolean isBeingCompared = false;

    @Override
    public int hashCode() {
        if (isBeingCompared) return 0;
        // Is this a good order independent hash? Probable not
        isBeingCompared = true;
        AtomicLong hash = new AtomicLong();
        this.forEachImmutable((key, value) -> hash.addAndGet(Objects.hash(key, value)));
        long longHash = hash.get();
        isBeingCompared = false;
        return (int)(longHash >> Long.numberOfTrailingZeros(longHash));
    }

    @Override
    public boolean equals(Object obj) {
        if (isBeingCompared) return true;
        isBeingCompared = true;
        try {
            if (obj instanceof CowCapableMap<?> otherMap) {
                if (otherMap == this) return true;
                return !this.findAny((key, value) -> !Objects.equals(otherMap.getImmutableErased(key), value));
            }
            return super.equals(obj);
        } finally {
            isBeingCompared = false;
        }
    }

    private static void checkGlobalCleaningQueue() {
        for (Reference<? extends CowCapableMap<?>> x; (x = GLOBAL_CLEANING_MAP.poll()) != null; ) {
            var ref = (CowWeakReference<?>)x;
            var children = ref.parent.children;
            if (children != null) {
                // Reïmplementation of `removeIf` to avoid allocations
                for (var i = 0; i < children.size(); i++) {
                    if (children.get(i).get() == null) {
                        children.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private static class CowWeakReference<T> extends WeakReference<CowCapableMap<T>> {
        @NotNull CowCapableMap<T> parent;
        public CowWeakReference(CowCapableMap<T> referent, ReferenceQueue<? super CowCapableMap<T>> q) {
            super(referent, q);
            assert referent.daddy != null;
            this.parent = referent.daddy;
        }
    }
}
