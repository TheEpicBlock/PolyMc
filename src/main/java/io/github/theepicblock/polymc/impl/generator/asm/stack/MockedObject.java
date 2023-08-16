package io.github.theepicblock.polymc.impl.generator.asm.stack;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.generator.asm.*;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public record MockedObject(@NotNull Origin origin, @Nullable VirtualMachine.Clazz type, CowCapableMap<String> overrides) implements StackEntry {
    public static final HashMap<String, StackEntry> MOCKED_RESOLVERS = new HashMap<>();

    public MockedObject(@NotNull Origin origin, @Nullable VirtualMachine.Clazz type) {
        this(origin, type, new CowCapableMap<>());
    }

    @Override
    public void setField(String name, StackEntry e) throws MethodExecutor.VmException {
        this.overrides.put(name, e);
    }

    @Override
    public boolean canBeSimplified() {
        if (MOCKED_RESOLVERS.isEmpty()) return false;
        if (this.origin instanceof Root) {
            return true;
        } else if (this.origin instanceof FieldAccess f) {
            return (f.root.canBeSimplified() || f.root.isConcrete());
        } else if (this.origin instanceof ArrayAccess a) {
            return (a.root.canBeSimplified() || a.root.isConcrete()) && (a.index.canBeSimplified() || a.index.isConcrete());
        } else if (this.origin instanceof MethodCall m) {
            return true;
        }

        throw new RuntimeException();
    }

    @Override
    public StackEntry simplify(VirtualMachine vm, Reference2ReferenceOpenHashMap<StackEntry,StackEntry> simplificationCache) throws MethodExecutor.VmException {
        if (MOCKED_RESOLVERS.isEmpty()) return this;
        if (this.origin instanceof Root r) {
            if (MOCKED_RESOLVERS.containsKey(r.name)) return MOCKED_RESOLVERS.get(r.name);
        } else if (this.origin instanceof FieldAccess f) {
            if (simplificationCache.containsKey(this)) return simplificationCache.get(this);
            var root = f.root;
            if (root.canBeSimplified()) root = root.simplify(vm, simplificationCache);

            if (root.isConcrete()) {
                return root.getField(f.fieldName);
            }
        } else if (this.origin instanceof ArrayAccess a) {
            if (simplificationCache.containsKey(this)) return simplificationCache.get(this);
            var root = a.root;
            if (root.canBeSimplified()) root = root.simplify(vm, simplificationCache);
            var i = a.index;
            if (i.canBeSimplified()) i = i.simplify(vm, simplificationCache);

            if (root.isConcrete() && i.isConcrete()) {
                return i.arrayAccess(i.extractAs(int.class));
            }
        } else if (this.origin instanceof MethodCall m) {
            if (simplificationCache.containsKey(this)) return simplificationCache.get(this);
            for (var i = 0; i < m.arguments.length; i++) {
                if (m.arguments[i] != null && m.arguments[i].canBeSimplified()) m.arguments[i] = m.arguments[i].simplify(vm, simplificationCache);
            }

            var a = Util.first(m.arguments);
            if (a != null && a.isConcrete()) {
                var state = vm.switchStack(null);
                try {
                    vm.getConfig().invoke(new VirtualMachine.Context(vm), m.currentClass, m.inst, m.arguments);
                    return vm.runToCompletion();
                } catch (MethodExecutor.VmException ignored) {}
                vm.switchStack(state);
            }
        }

        return this;
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

    @Override
    public void write(PacketByteBuf buf) {
        if (this.origin instanceof Root root) {
            buf.writeString("Root");
            buf.writeString(root.name);
        } else if (this.origin instanceof FieldAccess fieldAccess) {
            buf.writeString("FieldAccess");
            fieldAccess.root.writeWithTag(buf);
            buf.writeString(fieldAccess.fieldName());
        } else if (this.origin instanceof MethodCall methodCall) {
            buf.writeString("MethodCall");
            buf.writeString(methodCall.currentClass().name());
            buf.writeVarInt(methodCall.inst.getOpcode());
            buf.writeString(methodCall.inst.owner);
            buf.writeString(methodCall.inst.name);
            buf.writeString(methodCall.inst.desc);
            buf.writeVarInt(methodCall.arguments.length);
            for (var entry : methodCall.arguments) {
                buf.writeNullable(entry, (buf2, e) -> e.writeWithTag(buf2));
            }
        }
        buf.writeNullable(this.type, (buf2, obj2) -> buf2.writeString(obj2.name()));
        overrides.write(buf, PacketByteBuf::writeString);
    }

    public static VirtualMachine hehe = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {}); // TODO
    public static StackEntry read(PacketByteBuf buf) {
        var originType = buf.readString();
        Origin origin = switch (originType) {
            case "Root" -> new Root(buf.readString());
            case "FieldAccess" -> new FieldAccess(StackEntry.readWithTag(buf), buf.readString());
            case "MethodCall" -> {
                try {
                    var currentClazz = hehe.getClass(buf.readString());
                    var methodInsn = new MethodInsnNode(buf.readVarInt(), buf.readString(), buf.readString(), buf.readString());
                    var length = buf.readVarInt();
                    var args = new StackEntry[length];
                    for(int i = 0; i < length; i++) {
                        args[i] = buf.readNullable(StackEntry::readWithTag);
                    }
                    yield new MethodCall(currentClazz, methodInsn, args);
                } catch (MethodExecutor.VmException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> throw new RuntimeException("wdfiow");
        };
        var type = buf.readNullable((buf2) -> {
            try {
                return hehe.getClass(buf.readString());
            } catch (MethodExecutor.VmException e) {
                throw new RuntimeException(e);
            }
        });
        var overrides = CowCapableMap.readFromByteBuf(buf, PacketByteBuf::readString);
        return new MockedObject(origin, type, overrides);
    }

    public interface Origin {

    }

    public record Root(String name) implements Origin {

    }

    public record FieldAccess(@NotNull StackEntry root, String fieldName) implements Origin {

    }

    public static MockedObject methodCall(VirtualMachine.Clazz currentClass, @NotNull MethodInsnNode inst, @NotNull StackEntry[] arguments) throws MethodExecutor.VmException {
        return new MockedObject(new MethodCall(currentClass, inst, arguments), currentClass.getLoader().getType(Type.getReturnType(inst.desc)));
    }

    public record MethodCall(VirtualMachine.Clazz currentClass, @NotNull MethodInsnNode inst, @NotNull StackEntry[] arguments) implements Origin {

        @Override
        public String toString() {
            return "MethodCall["+inst.owner+"#"+inst.name+"]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodCall that = (MethodCall)o;
            return Objects.equals(currentClass, that.currentClass) && instEquals(inst, that.inst) && Arrays.deepEquals(arguments, that.arguments);
        }

        private static boolean instEquals(@NotNull MethodInsnNode a, @NotNull MethodInsnNode b) {
            if (a == b) return true;
            return a.getOpcode() == b.getOpcode() && Objects.equals(a.name, b.name) && Objects.equals(a.owner, b.owner) && Objects.equals(a.desc, b.desc);
        }

        @Override
        public int hashCode() {
            var instHash = Objects.hash(inst.owner, inst.name, inst.desc, inst.getOpcode());
            int result = Objects.hash(currentClass, instHash);
            result = 31 * result + Arrays.deepHashCode(arguments);
            return result;
        }
    }

    public record ArrayAccess(StackEntry root, StackEntry index) implements Origin {
        
    }
}
