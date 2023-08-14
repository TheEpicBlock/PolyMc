package io.github.theepicblock.polymc.impl.generator.asm.stack;

import com.google.gson.JsonElement;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents and object that exists outside the vm
 */
public record KnownObject(Object i, @NotNull HashMap<Object, StackEntry> mutations) implements StackEntry {
    public static KnownObject NULL = new KnownObject(null, new HashMap<>());

    public KnownObject(Object i) {
        this(i, new HashMap<>());
    }

    @Override
    public JsonElement toJson() {
        if (!mutations.isEmpty()) throw new NotImplementedException("Known object extraction does not yet factor in mutations");
        return StackEntry.GSON.toJsonTree(i);
    }

    @Override
    public <T> T extractAs(Class<T> type) {
        if (!mutations.isEmpty()) throw new NotImplementedException("Known object extraction does not yet factor in mutations");
        if (i == null && type.isPrimitive()) {
            // Primitive types shouldn't be null
            throw new ClassCastException("Can't extract known null as "+type);
        }
        if (i == null) return null;
        if (type.isAssignableFrom(i.getClass())) {
            return (T)i;
        }
        return StackEntry.super.extractAs(type);
    }

    @Override
    public @NotNull StackEntry getField(String name) throws VmException {
        if (mutations.containsKey(name)) {
            return mutations.get(name);
        }
        try {
            var clazz = i.getClass();
            var field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return StackEntry.known(field.get(i));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            if (name.equals("ordinal") && i instanceof Enum<?> enumm) {
                return StackEntry.known(enumm.ordinal());
            }
            throw new VmException("Couldn't get field "+name, e);
        }
    }

    @Override
    public void setField(String name, StackEntry e) throws VmException {
        if (this.i == null) {
            throw new VmException("Can't set property on null value", null);
        }
        mutations.put(name, e);
    }

    @Override
    public @NotNull StackEntry arrayAccess(int index) throws VmException {
        if (i instanceof Object[] arr) {
            if (mutations.containsKey(index)) {
                return mutations.get(index);
            }
            if (arr[index] == null) {
                return KnownObject.NULL;
            }
            return new KnownObject(arr[index]);
        } else if (i instanceof int[] arr) {
            if (mutations.containsKey(index)) {
                return mutations.get(index);
            }
            return new KnownObject(arr[index]);
        } else if (i instanceof long[] arr) {
            if (mutations.containsKey(index)) {
                return mutations.get(index);
            }
            return new KnownObject(arr[index]);
        } else {
            throw new VmException("Attempted to use "+this+" as an array "+this.getClass(), null);
        }
    }

    @Override
    public void arraySet(int index, @NotNull StackEntry entry) throws VmException {
        mutations.put(index, entry);
    }

    @Override
    public StackEntry[] asKnownArray() {
        if ((i instanceof int[] a && a.length == 0) ||
            (i instanceof long[] b && b.length == 0) ||
            (i instanceof double[] c && c.length == 0) ||
            (i instanceof Object[] d && d.length == 0)) {
            return new StackEntry[0];
        }
        return StackEntry.super.asKnownArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeMap(mutations,
                (buf2, key) -> {
                    if (key instanceof Integer integer) {
                        buf.writeBoolean(true);
                        buf.writeVarInt(integer);
                    } else {
                        buf.writeBoolean(false);
                        buf.writeString((String)key);
                    }
                },
                (buf2, entry) -> entry.writeWithTag(buf2));
        buf.writeNullable(i, (buf2, obj2) -> {
            buf2.writeString(obj2.getClass().getName());
            buf2.writeString(GSON.toJson(i));
        });
    }

    public static StackEntry read(PacketByteBuf buf) {
        var mutations = buf.<Object, StackEntry, HashMap<Object, StackEntry>>readMap(
                HashMap::new,
                (buf2) -> {
                    if (buf2.readBoolean()) {
                        return buf2.readVarInt();
                    } else {
                        return buf2.readString();
                    }
                },
                StackEntry::readWithTag
        );
        var i = buf.readNullable(buf2 -> {
            try {
                var clazz = Class.forName(buf.readString());
                return GSON.fromJson(buf.readString(), clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Type not found when deserializing object", e);
            }
        });
        return new KnownObject(i, mutations);
    }

    @Override
    public boolean isConcrete() {
        return true;
    }
}