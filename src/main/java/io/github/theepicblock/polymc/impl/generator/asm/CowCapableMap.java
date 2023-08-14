package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class CowCapableMap<T> {
    private static final int INVALID_KEYCODE = 0;
    private static final ReferenceQueue<CowCapableMap<?>> GLOBAL_CLEANING_MAP = new ReferenceQueue<>();
    private static final boolean DEBUG_MODIFICATIONS = false;//FabricLoader.getInstance().isDevelopmentEnvironment();
    private @Nullable CowCapableMap<T> daddy = null;
    private @Nullable Map<T, @Nullable StackEntry> overrides;
    private int keyCode = INVALID_KEYCODE;
    private ModificationData modDebugger;

    /**
     * Should be non-null if {@link #daddy} is non-null
     */
    private Reference2ReferenceOpenHashMap<StackEntry, StackEntry> copyCache;

    public CowCapableMap() {
        this.overrides = new Object2ObjectOpenHashMap<>();
        if (DEBUG_MODIFICATIONS) {
            this.modDebugger = new ModificationData();
        }
    }

    /**
     * SAFETY: once a clone is made, the original shouldn't be interacted with until the clone is disposed off
     */
    @TestOnly
    public CowCapableMap<T> createClone() {
        return createClone(new Reference2ReferenceOpenHashMap<>());
    }

    /**
     * SAFETY: once a clone is made, the original shouldn't be interacted with until the clone is disposed off
     */
    public CowCapableMap<T> createClone(Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        var child = new CowCapableMap<T>();
        child.clearAndCopy(this, copyCache);
        return child;
    }

    /**
     * SAFETY: once a clone is made, the original shouldn't be interacted with until the clone is disposed off
     */
    public void clearAndCopy(@NotNull CowCapableMap<T> daddy, @NotNull Reference2ReferenceOpenHashMap<StackEntry,StackEntry> copyCache) {
        this.daddy = daddy;
        this.overrides = null;
        this.copyCache = copyCache;
        if (DEBUG_MODIFICATIONS) {
            this.modDebugger.parentExpected = daddy.modDebugger.modCount;
        }
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
        checkParentForModifications();
        if (overrides != null) {
            if (overrides.containsKey(key)) {
                return overrides.get(key);
            }
        }
        var nextDad = this.getDaddyWithDifferentKeys();
        if (nextDad != null) {
            var o = nextDad.getImmutable(key);
            if (o != null) {
                incrementModifications();
                var copy = o.copy(this.copyCache);
                if (copy != o) {
                    if (this.overrides == null) this.overrides = new HashMap<>();
                    this.overrides.put(key, copy);
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
        checkParentForModifications();
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

    public void put(T key, @NotNull StackEntry value) {
        if (overrides == null) overrides = new HashMap<>();
        keyCode = INVALID_KEYCODE;
        incrementModifications();
        checkParentForModifications();
        overrides.put(key, value);
    }

    public void putAll(@NotNull Map<? extends T,? extends StackEntry> m) {
        m.forEach(this::put);
    }

    public void simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry, StackEntry> simplificationCache) throws MethodExecutor.VmException {
        checkParentForModifications();
        if (this.overrides != null) {
            for (var entry : overrides.entrySet()) {
                if (entry.getValue() == null) continue;
                incrementModifications();
                overrides.put(entry.getKey(), entry.getValue().simplify(vm, simplificationCache));
            }
        }
        // If the dad has the same keys then it won't affect this map anyway, so we can skip those
        var nextDad = this.getDaddyWithDifferentKeys();
        if (nextDad != null) {
            nextDad.simplify(vm, simplificationCache);
        }
    }

    public Map<T, StackEntry> copyToRegularMap() {
        var map = new HashMap<T, StackEntry>();
        this.forEachImmutable(map::put);
        return map;
    }

    public void write(PacketByteBuf buf, PacketByteBuf.PacketWriter<T> keyWriter) {
        buf.writeMap(this.copyToRegularMap(),
                keyWriter,
                (buf2, val) -> val.writeWithTag(buf2));
    }

    public static <T> CowCapableMap<T> readFromByteBuf(PacketByteBuf buf, PacketByteBuf.PacketReader<T> keyReader) {
        int size = buf.readVarInt();
        var map = new CowCapableMap<T>();

        for(int i = 0; i < size; ++i) {
            map.put(keyReader.apply(buf), StackEntry.readWithTag(buf));
        }

        return map;
    }

    public void forEachImmutable(BiConsumer<? super T,? super StackEntry> action) {
        checkParentForModifications();
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
        checkParentForModifications();
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

    private int getKeyHashCode() {
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

    private static class CowWeakReference<T> extends WeakReference<CowCapableMap<T>> {
        @NotNull CowCapableMap<T> parent;
        public CowWeakReference(CowCapableMap<T> referent, ReferenceQueue<? super CowCapableMap<T>> q) {
            super(referent, q);
            assert referent.daddy != null;
            this.parent = referent.daddy;
        }
    }

    private void incrementModifications() {
        if (DEBUG_MODIFICATIONS) this.modDebugger.modCount++;
    }

    /**
     * Do a best-effort check to see if the parent was changed since making the clone.
     * If the parent changed, that could've also influenced this map
     * @see #createClone(Reference2ReferenceOpenHashMap)
     */
    private void checkParentForModifications() {
        if (DEBUG_MODIFICATIONS && this.daddy != null) {
            if (this.modDebugger.parentExpected != daddy.modDebugger.modCount) {
                throw new CowParentModification("Parent has been modified whilst clone was still in use. ("+this.modDebugger.parentExpected+" != "+daddy.modDebugger.modCount+")");
            }
        }
    }

    private static class ModificationData {
        public int modCount;
        public int parentExpected = -1;
    }

    public static class CowParentModification extends RuntimeException {
        public CowParentModification(String message) {
            super(message);
        }
    }
}
