package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownArray;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class CowCapableMap<T> {
    private static final ReferenceQueue<CowCapableMap<?>> GLOBAL_CLEANING_MAP = new ReferenceQueue<>();
    private @Nullable CowCapableMap<T> daddy = null;
    private @Nullable Map<T, @Nullable StackEntry> overrides = null;
    private @Nullable HashSet<CowWeakReference<T>> children = null;
    private static int INVALID_KEYCODE;
    private int keyCode = INVALID_KEYCODE;

    public CowCapableMap() {
        this.overrides = new Object2ObjectOpenHashMap<>();
    }

    private CowCapableMap(@NotNull CowCapableMap<T> daddy) {
        this.daddy = daddy;
    }

    public CowCapableMap<T> createClone() {
        return createClone(new Reference2ReferenceOpenHashMap<>());
    }

    public CowCapableMap<T> createClone(Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        var child = new CowCapableMap<T>();
        child.clearAndCopy(this, copyCache);
        return child;
    }

    public void clearAndCopy(@NotNull CowCapableMap<T> daddy, @NotNull Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        this.daddy = daddy;
        this.overrides = null;
        this.forEachImmutable((key,val) -> {
            if (val instanceof KnownVmObject || val instanceof KnownArray) {
                this.put(key, val.copy(copyCache));
            }
        });
        if (daddy.children == null) daddy.children = new HashSet<>();
        daddy.children.add(new CowWeakReference<>(this, GLOBAL_CLEANING_MAP));
    }

    public void clearAndCopy(@NotNull CowCapableMap<T> daddy, List<T> nonPrimitiveFields, @NotNull Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        this.daddy = daddy;
        this.overrides = null;
//        for (var field : nonPrimitiveFields) {
//            var e = daddy.getImmutable(field);
//            if (e != null) {
//                var copy = e.copy(copyCache);
//                if (copy == e) continue; // This is an immutable value
//                this.put(field, e.copy(copyCache));
//            }
//        }
        if (daddy.children == null) daddy.children = new HashSet<>();
        daddy.children.add(new CowWeakReference<>(this, GLOBAL_CLEANING_MAP));
    }

    public boolean isEmpty() {
        boolean daddyEmpty = true;
        if (daddy != null) {
            daddyEmpty = daddy.isEmpty();
        }

        boolean overridesEmpty = true;
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
            if (overrides.containsKey(key)) {
                var o = overrides.get(key);
                if (o != null) {
                    var copy = o.copy(); // Yeah, this copy is pretty useless, but it prevents some CME's when the map is recursive
                    if (copy != o) {
                        copyToChildren(key, copy);
                    }
                }
                return o;
            }
        }
        var nextDad = this.getDaddyWithDifferentKeys();
        if (nextDad != null) {
            var o = nextDad.get(key);
            if (o != null) {
                var copy = o.copy();
                if (copy != o) {
                    if (this.overrides == null) this.overrides = new HashMap<>();
                    this.overrides.put(key, copy);
                    copyToChildren(key, copy);
                }
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
            if (overrides.containsKey(key)) {
                return overrides.get(key);
            }
        }
        var nextDad = this.getDaddyWithDifferentKeys();
        if (nextDad != null) {
            return nextDad.getImmutable(key);
        }
        return null;
    }

    private StackEntry getImmutableErased(Object key) {
        return getImmutable((T)key);
    }

    @Nullable
    public StackEntry put(T key, @NotNull StackEntry value) {
        if (overrides == null) overrides = new HashMap<>();
        keyCode = INVALID_KEYCODE;
        // If the children are null there's no reason to compute the previous value
        var previous = children == null ? null : this.getImmutable(key);
        overrides.put(key, value);
        copyToChildren(key, previous);
        return previous;
    }

    private void copyToChildren(@NotNull T key, @Nullable StackEntry v) {
        var c = children;
        if (c != null) {
            for (var weakReference : c) {
                var child = weakReference.get();
                if (child != null) {
                    if (child.overrides == null) child.overrides = new Object2ObjectOpenHashMap<>();
                    child.overrides.computeIfAbsent(key, (a) -> {
                        child.keyCode = INVALID_KEYCODE;
                        return v == null ? null : v.copy();
                    });
                }
            }
        }
    }

    private void iterChildren(Consumer<CowCapableMap<T>> childConsumer) {
        var c = children;
        if (c != null) {
            for (var weakReference : c) {
                var child = weakReference.get();
                if (child != null) {
                    childConsumer.accept(child);
                }
            }
        }
    }

    public void putAll(@NotNull Map<? extends T,? extends StackEntry> m) {
        m.forEach(this::put);
    }

    public void simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry, StackEntry> simplificationCache) throws MethodExecutor.VmException {
        if (this.overrides != null) {
            for (var entry : overrides.entrySet()) {
                if (entry.getValue() == null) continue;
                // No need to notify children since a simplification shouldn't change the meaning of the value
                overrides.put(entry.getKey(), entry.getValue().simplify(vm, simplificationCache));
            }
        }
        // If the dad has the same keys then it won't affect this map anyway, so we can skip those
        var nextDad = this.getDaddyWithDifferentKeys();
        if (nextDad != null) {
            nextDad.simplify(vm, simplificationCache);
        }
    }

    public void forEachImmutable(BiConsumer<? super T,? super StackEntry> action) {
        if (this.overrides != null) {
            for (var entry : this.overrides.entrySet()) {
                var k = entry.getKey();
                var value = entry.getValue();
                if (value != null) {
                    // Null indicates that this entry doesn't exist in the map. It shouldn't be exposed
                    action.accept(k, value);
                }
            }
            // If the dad has the same keys then it won't affect this map anyway, so we can skip those
            var nextDad = this.getDaddyWithDifferentKeys();
            if (nextDad != null) {
                nextDad.forEachImmutable((key, val) -> {
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
                // Null indicates that this entry doesn't exist in the map. It shouldn't be exposed
                if (entry.getValue() == null) continue;
                if (filter.applyAsBoolean(entry.getKey(), entry.getValue())) {
                    return true;
                }
            }
            // If the dad has the same keys then it won't affect this map anyway, so we can skip those
            var nextDad = this.getDaddyWithDifferentKeys();
            if (nextDad != null) {
                return nextDad.findAny((key, val) -> !overrides.containsKey(key) && filter.applyAsBoolean(key, val));
            }
        } else if (this.daddy != null) {
            return this.daddy.findAny(filter);
        }
        return false;
    }

    private @Nullable CowCapableMap<T> getDaddyWithDifferentKeys() {
        var c = this;
        while (true) {
            var d = c.daddy;
            if (d == null) return null;
            var cSize = c.overrides == null ? 0 : c.overrides.size();
            var dSize = d.overrides == null ? 0 : d.overrides.size();
            if (cSize != dSize && c.getKeyHashCode() != d.getKeyHashCode()) {
                return d;
            }
            c = d;
        }
    }

    public int getKeyHashCode() {
        if (this.keyCode == INVALID_KEYCODE) {
            if (this.overrides != null) {
                int result = 1;
                for (var key : this.overrides.keySet()) {
                    result = 31 * result + (key == null ? 0 : key.hashCode());
                }
                this.keyCode = result;
            } else {
                this.keyCode = 1;
            }
        }
        return this.keyCode;
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
                    children.remove(ref);
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

    public static CleanerThread spawnCleanerThread() {
        var thread = new CleanerThread();
        thread.setDaemon(true);
        thread.setName("PolyMc Vm Cleaner");
        thread.start();
        return thread;
    }

    public static class CleanerThread extends Thread {
        public boolean stopped = false;
        @Override
        public void run() {
            while (!stopped) {
                checkGlobalCleaningQueue();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
