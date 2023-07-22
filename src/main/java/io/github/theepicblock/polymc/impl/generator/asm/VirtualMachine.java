package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.common.collect.Streams;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.UnaryArbitraryOp;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VirtualMachine {
    private final HashMap<@InternalName String, Clazz> classes = new HashMap<>();
    private final ClientClassLoader classResolver;
    private VmConfig config;
    private AbstractObjectList<@NotNull MethodExecutor> methodStack = new ObjectArrayList<>();
    private StackEntry lastReturnedValue;

    public VirtualMachine(ClientClassLoader classResolver, VmConfig config) {
        this.classResolver = classResolver;
        this.config = config;

        this.init();
    }

    /**
     * Internal use for copied vm's
     */
    private VirtualMachine(ClientClassLoader classResolver, VmConfig config, StackEntry lastReturnedValue) {
        this.classResolver = classResolver;
        this.config = config;
        this.lastReturnedValue = lastReturnedValue;
    }

    public void changeConfig(VmConfig config) {
        this.config = config;
    }

    public VirtualMachine copy() {
        var n = new VirtualMachine(this.classResolver, config, lastReturnedValue);
        for (var meth : this.methodStack) {
            n.methodStack.add(meth.copy(n));
        }
        return n;
    }

    private void init() {
        var state = this.switchStack(null);
        try {
            this.addMethodToStack(_System, "setJavaLangAccess", "()V");
            this.runToCompletion();
        } catch (VmException e) {
            PolyMc.LOGGER.warn("Error initializing VM: "+e.createFancyErrorMessage());
        }
        this.switchStack(state);
    }

    public AbstractObjectList<@NotNull MethodExecutor> switchStack(@Nullable AbstractObjectList<@NotNull MethodExecutor> newStack) {
        if (newStack == null) {
            newStack = new ObjectArrayList<>();
        }
        var oldStack = this.methodStack;
        this.methodStack = newStack;
        return oldStack;
    }

    @ApiStatus.Internal
    public MethodExecutor inspectRunningMethod() {
        return this.methodStack.top();
    }

    /**
     * Should be called by the {@link MethodExecutor} that's currently on top of the stack
     */
    @ApiStatus.Internal
    protected void onMethodReturn(@Nullable StackEntry returnValue) {
        this.lastReturnedValue = returnValue;
        methodStack.pop(); // The top method returned a value now, so it shall be yeeted

        if (!methodStack.isEmpty()) {
            methodStack.top().receiveReturnValue(returnValue);
        }
    }

    /**
     * Adds a lambda method to the stack and then runs the vm to completion
     */
    public StackEntry runLambda(Lambda lambda, StackEntry[] arguments) throws VmException {
        var method = lambda.method();
        var clazz = getClass(method.getOwner());
        if (method.getTag() == Opcodes.H_NEWINVOKESPECIAL) {
            var newO = new StackEntry[] { new KnownVmObject(clazz, new CowCapableMap<>()) };
            StackEntry[] args = Streams
                    .concat(Arrays.stream(newO), Arrays.stream(arguments), Arrays.stream(lambda.extraArguments()))
                    .toArray(StackEntry[]::new);
            addMethodToStack(clazz, method.getName(), method.getDesc(), args);
            this.runToCompletion();
            return newO[0];
        } else {
            StackEntry[] args = Streams.concat(Arrays.stream(arguments), Arrays.stream(lambda.extraArguments()))
                    .toArray(StackEntry[]::new);
            addMethodToStack(clazz, method.getName(), method.getDesc(), args);
            return this.runToCompletion();
        }
    }

    public void addMethodToStack(@InternalName String clazz, String method, String desc) throws VmException {
        var clazzNode = getClass(clazz);
        addMethodToStack(clazzNode, method, desc, null);
    }

    public void addMethodToStack(Clazz clazz, String method, String desc, @Nullable StackEntry[] arguments)
            throws VmException {
        var meth = clazz.getMethod(method, desc);
        if (meth == null) {
            throw new VmException(
                    "Couldn't find method `" + method + "` with desc `" + desc + "` in class `" + clazz.node.name + "`",
                    null);
        }
        addMethodToStack(new MethodRef(clazz, meth), arguments);
    }

    public void addMethodToStack(MethodRef methRef, @Nullable StackEntry[] arguments) throws VmException {
        var meth = methRef.meth();
        var a = arguments == null ? -1 : arguments.length;
        var localVariables = new StackEntry[Math.max(meth.maxLocals, a)];

        // Fill in arguments
        if (arguments != null) {
            int i = 0;
            for (var pair : arguments) {
                localVariables[i] = pair;
                i++;
            }
        }

        var executor = new MethodExecutor(this, localVariables,
                methRef.clazz().node.name + "#" + meth.name + meth.desc);
        methodStack.push(executor);
        executor.setMethod(meth.instructions, methRef.clazz());
    }

    public StackEntry runToCompletion() throws VmException {
        while (!this.methodStack.isEmpty()) {
            var top = this.methodStack.top();
            try {
                top.startExecution();
            } catch (VmException e) {
                if (methodStack.isEmpty()) {
                    var newException = new VmException("Exception with empty stack (likely due to stack switch)", e);
                    this.config.onVmError(top.getName(), top.getName().endsWith("v"), newException);
                } else {
                    var handledVal = this.config.onVmError(top.getName(), top.getName().endsWith("v"), e);
                    this.onMethodReturn(handledVal);
                }
            }
        }
        return this.lastReturnedValue;
    }

    public Clazz getClass(@InternalName @NotNull String name) throws VmException {
        var clazz = this.classes.get(name);
        if (clazz == null) {

            // Load class using ASM
            try {
                var node = classResolver.getClass(name);
                clazz = new Clazz(node, this);
                this.classes.put(name, clazz);
            } catch (IOException e) {
                throw new VmException("Error loading " + name, e);
            }
        }
        return clazz;
    }

    public VmConfig getConfig() {
        return config;
    }

    public ClientClassLoader getClassResolver() {
        return classResolver;
    }

    public void ensureClinit(Clazz node) throws VmException {
        // This isn't spec-compliant
        if (!node.hasInitted) {
            node.hasInitted = true;
            try {
                var stack = this.switchStack(null); // Run this in a new, fresh state
                addMethodToStack(node, "<clinit>", "()V", null);
                runToCompletion();
                this.switchStack(stack); // Restore old state
            } catch (VmException e) {
                throw new VmException("Error in clinit of "+node, e);
            }
        }
    }

    public Clazz getType(StackEntry entry) throws VmException {
        if (entry instanceof KnownObject o) {
            if (o.i() == null) {
                throw new VmException("VM-NPE", null);
            }
            return this.getClass(Type.getInternalName(o.i().getClass()));
        } else if (entry instanceof KnownVmObject o) {
            return o.type();
        } else if (entry instanceof KnownClass) {
            return this.getClass("java/lang/Class");
        } else if (entry instanceof KnownInteger) {
            return this.getClass(_Integer);
        } else if (entry instanceof KnownLong) {
            return this.getClass(_Long);
        } else if (entry instanceof KnownFloat) {
            return this.getClass(_Float);
        } else if (entry instanceof KnownDouble) {
            return this.getClass(_Double);
        } else {
            return null;
        }
    }

    /**
     * @param currentClass The class the method is in
     * @return null if the {@code objectRef} is of an unknown type, and the method
     *         can't be resolved due to that.
     */
    public @Nullable MethodRef resolveMethod(Clazz currentClass, MethodInsnNode inst,
            @Nullable StackEntry objectRef) throws VmException {
        switch (inst.getOpcode()) {
            case Opcodes.INVOKESTATIC -> {
                var clazz = this.getClass(inst.owner);
                var method = clazz.getMethod(inst.name, inst.desc);
                // This is a hard-error. Static methods shouldn't be hard to find and
                // something's wrong here. So no returning null in this case
                if (method == null)
                    throw new VmException("Couldn't find static method " + inst.name + inst.desc + " in " + inst.owner,
                            null);
                if (AsmUtils.hasFlag(method, Opcodes.ACC_NATIVE)) {
                    throw new VmException("Static method " + inst.name + inst.desc + " in "
                            + clazz + " (" + inst.getOpcode() + ", " + inst.owner + ") resolved to a native method", null);
                }
                return new MethodRef(clazz, method);
            }
            case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL -> {
                if (objectRef == null)
                    throw new IllegalArgumentException(
                            "objectRef can't be null for method invocation (" + inst.getOpcode() + ")");

                // Find the root class from which to start looking for the method
                Clazz rootClass = switch (inst.getOpcode()) {
                    case Opcodes.INVOKESPECIAL -> {
                        // See
                        // https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-6.html#jvms-6.5.invokespecial
                        var clazz = this.getClass(inst.owner);
                        if (!inst.name.startsWith("<init>") &&
                                !AsmUtils.hasFlag(clazz.node, Opcodes.ACC_INTERFACE) &&
                                // class must be a superclass of the current class
                                AsmUtils.getInheritanceChain(currentClass).stream().skip(1).anyMatch(classNode -> classNode.name.equals(inst.owner)) &&
                                AsmUtils.hasFlag(currentClass.node, Opcodes.ACC_SUPER)) {
                            yield this.getClass(currentClass.node.superName);
                        } else {
                            yield clazz;
                        }
                    }
                    case Opcodes.INVOKEINTERFACE, Opcodes.INVOKEVIRTUAL -> getType(objectRef);
                    default -> throw new IllegalStateException();
                };

                if (rootClass == null)
                    return null;

                // Step 2 of method resolution (done recursively for each superclass)
                // See https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-5.html#jvms-5.4.3.3
                var clazz = rootClass;
                while (true) {
                    var method = clazz.getMethod(inst.name, inst.desc);
                    if (method != null) {
                        if (AsmUtils.hasFlag(method, Opcodes.ACC_NATIVE)) {
                            throw new VmException("Method " + inst.name + inst.desc + " in "
                                    + rootClass + " (" + inst.getOpcode() + ", " + inst.owner + ") resolved to a native method", null);
                        }
                        if (AsmUtils.hasFlag(method, Opcodes.ACC_ABSTRACT)) {
                            throw new VmException("Method " + inst.name + inst.desc + " in "
                                    + rootClass + " (" + inst.getOpcode() + ", " + inst.owner + ") resolved to an abstract method", null);
                        }
                        return new MethodRef(clazz, method);
                    } else {
                        // Check super class
                        if (clazz.node.superName == null) break;
                        clazz = this.getClass(clazz.node.superName);
                    }
                }

                // Step 3 of method resolution
                if (inst.getOpcode() == Opcodes.INVOKEINTERFACE || inst.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    clazz = rootClass; // Reset back to the root class
                    while (true) {
                        var method = AsmUtils.forEachInterface(clazz, interfaceClass -> {
                            var defaultMethod = interfaceClass.getMethod(inst.name, inst.desc);
                            if (defaultMethod != null &&
                                    !AsmUtils.hasFlag(defaultMethod, Opcodes.ACC_ABSTRACT) &&
                                    !AsmUtils.hasFlag(defaultMethod, Opcodes.ACC_STATIC) &&
                                    !AsmUtils.hasFlag(defaultMethod, Opcodes.ACC_PRIVATE))
                                return new MethodRef(interfaceClass, defaultMethod);
                            return null;
                        });
                        if (method != null) return method;
                        if (clazz.node.superName == null) break;
                        clazz = this.getClass(clazz.node.superName);
                    }
                }


                throw new VmException("Can't find method " + inst.name + inst.desc + " in "
                        + rootClass.node.name + " (" + inst.getOpcode() + ", " + inst.owner + ")", null);
            }
            default -> throw new UnsupportedOperationException("instruction shouldn't have an opcode of "+inst.getOpcode());
        }
    }

    private static final @InternalName String LoggerFactory = Type.getInternalName(LoggerFactory.class);
    private static final @InternalName String String = Type.getInternalName(String.class);
    private static final @InternalName String Objects = Type.getInternalName(Objects.class);
    private static final @InternalName String _Arrays = Type.getInternalName(Arrays.class);
    private static final @InternalName String _StrictMath = Type.getInternalName(StrictMath.class);
    private static final @InternalName String _Float = Type.getInternalName(Float.class);
    private static final @InternalName String _Double = Type.getInternalName(Double.class);
    private static final @InternalName String _VM = "jdk/internal/misc/VM";
    private static final @InternalName String _Class = Type.getInternalName(Class.class);
    private static final @InternalName String _System = Type.getInternalName(System.class);
    private static final @InternalName String _Runtime = Type.getInternalName(Runtime.class);
    private static final @InternalName String _Unsafe = "jdk/internal/misc/Unsafe";
    private static final @InternalName String _SharedSecrets = "jdk/internal/access/SharedSecrets";
    private static final @InternalName String _CDS = "jdk/internal/misc/CDS";
    private static final @InternalName String _Reflection = "jdk/internal/reflect/Reflection";
    private static final @InternalName String _Integer = Type.getInternalName(Integer.class);
    private static final @InternalName String _Long = Type.getInternalName(Long.class);

    public interface VmConfig {
        default @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            return clazz.getStatic(inst.name);
        }

        default StackEntry onVmError(String method, boolean returnsVoid, VmException e) throws VmException {
            throw e;
        }

        default void putStaticField(Context ctx, FieldInsnNode inst, StackEntry value) throws VmException {
            var clazz = ctx.machine.getClass(inst.owner);
            ctx.machine.ensureClinit(clazz);
            clazz.setStatic(inst.name, value);
        }

        /**
         * @param currentClass Class the invocation is coming from
         * @param arguments    If this instruction is not invokeStatic, the first element
         *                     will represent the object this was called on.
         */
        default void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments)
                throws VmException {
            // Auto-boxing
            if (inst.getOpcode() != Opcodes.INVOKESTATIC &&
                    (Util.first(arguments) instanceof KnownInteger ||
                    Util.first(arguments) instanceof KnownLong ||
                    Util.first(arguments) instanceof KnownDouble ||
                    Util.first(arguments) instanceof KnownFloat)) {
                var boxedObject = new KnownVmObject(ctx.machine().getClass(inst.owner));
                boxedObject.setField("value", arguments[0]);
                arguments[0] = boxedObject;
            }
            if (inst.owner.equals(LoggerFactory)) {
                ret(ctx, new UnknownValue("Refusing to invoke LoggerFactory for optimization reasons"));
                return;
            }
            // Hardcoded functions
            if (inst.owner.equals(Objects) && inst.name.equals("requireNonNull")) {
                ret(ctx, arguments[0]);
                return;
            }
            if (inst.owner.equals(_Arrays) && inst.name.equals("copyOf")
                    && inst.desc.equals("([Ljava/lang/Object;I)[Ljava/lang/Object;")
                    && arguments[0] instanceof KnownArray a) {
                ret(ctx, new KnownArray(Arrays.copyOf(a.data(), arguments[1].extractAs(Integer.class))));
                return;
            }
            if (inst.owner.equals(_StrictMath)) {
                // These are native methods. They need to be hardcoded
                if (inst.name.equals("cos") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.cos(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("acos") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.acos(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("sin") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.sin(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("asin") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.asin(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("tan") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.tan(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("atan") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.atan(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("log") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.log(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("log10") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.log10(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("sqrt") && inst.desc.equals("(D)D")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(StrictMath.sqrt(entry.extractAs(Double.class)))));
                    return;
                }
            }
            if (inst.owner.equals(_Float)) {
                if (inst.name.equals("floatToRawIntBits")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(Float.floatToRawIntBits(entry.extractAs(Float.class)))));
                    return;
                }
                if (inst.name.equals("intBitsToFloat")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(Float.intBitsToFloat(entry.extractAs(Integer.class)))));
                    return;
                }
            }
            if (inst.owner.equals(_Double)) {
                if (inst.name.equals("doubleToRawLongBits")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(Double.doubleToRawLongBits(entry.extractAs(Double.class)))));
                    return;
                }
                if (inst.name.equals("longBitsToDouble")) {
                    ret(ctx, new UnaryArbitraryOp(arguments[0], entry -> StackEntry.known(Double.longBitsToDouble(entry.extractAs(Long.class)))));
                    return;
                }
            }
            if (inst.owner.equals(_VM)) {
                if (inst.name.equals("getSavedProperty")) {
                    if (arguments[0] instanceof KnownObject o && o.i() instanceof String str && str.equals("java.lang.Integer.IntegerCache.high")) {
                        ret(ctx, new KnownObject("127"));
                        return;
                    }
                }
            }
            if (inst.owner.equals(_Class)) {
                if (inst.name.equals("desiredAssertionStatus")) {
                    ret(ctx, new KnownInteger(false));
                    return;
                }
                if (inst.name.equals("getEnumConstantsShared") && Util.first(arguments) instanceof KnownClass cl) {
                    var clazz = ctx.machine.getClass(cl.type().getInternalName());
                    ret(ctx, clazz.getStatic("$VALUES"));
                    return;
                }
                if (inst.name.equals("isInterface") && Util.first(arguments) instanceof KnownClass cl) {
                    var clazz = ctx.machine.getClass(cl.type().getInternalName());
                    ret(ctx, new KnownInteger(AsmUtils.hasFlag(clazz.node, Opcodes.ACC_INTERFACE)));
                    return;
                }
                if (inst.name.equals("isArray") && Util.first(arguments) instanceof KnownClass cl) {
                    ret(ctx, new KnownInteger(cl.type().getSort() == Type.ARRAY));
                    return;
                }
                if (inst.name.equals("isPrimitive") && Util.first(arguments) instanceof KnownClass cl) {
                    ret(ctx, new KnownInteger(cl.type().getSort() != Type.OBJECT && cl.type().getSort() != Type.ARRAY));
                    return;
                }
                if (inst.name.equals("getSuperclass") && Util.first(arguments) instanceof KnownClass cl) {
                    ret(ctx, new KnownClass(ctx.machine.getClass(ctx.machine.getClass(cl.type().getInternalName()).node.superName)));
                    return;
                }
            }
            if (inst.owner.equals(_CDS)) {
                if (inst.name.equals("initializeFromArchive")) {
                    ret(ctx, null); // Just pretend it can't be initialized. I don't even know what a CDS dump is
                    return;
                }
                if (inst.name.equals("getRandomSeedForDumping")) {
                    ret(ctx, new KnownLong(4)); // Chosen by fair dice roll, guaranteed to be random
                    return;
                }
            }
            if (inst.owner.equals(_Reflection)) {
                if (inst.name.equals("getCallerClass")) {
                    ret(ctx, new KnownClass(currentClass));
                    return;
                }
            }
            if (inst.owner.equals(_Unsafe)) {
                if (inst.name.equals("registerNatives")) {
                    ret(ctx, null);
                    return;
                }
                if (inst.name.equals("objectFieldOffset") && inst.desc.equals("(Ljava/lang/Class;Ljava/lang/String;)J")) {
                    ret(ctx, new UnsafeFieldReference(arguments[2].extractAs(String.class)));
                    return;
                }
                if (inst.name.equals("arrayIndexScale")) {
                    ret(ctx, new KnownInteger(1)); // Keeps everything nice and simple
                    return;
                }
                if (inst.name.equals("arrayBaseOffset")) {
                    ret(ctx, new KnownInteger(0)); // Keeps everything nice and simple
                    return;
                }
                if (inst.name.startsWith("get") && inst.desc.startsWith("(Ljava/lang/Object;J)") && arguments[1] instanceof KnownArray arr) {
                    // Arrays may use normal longs as indices
                    if (arguments[2].canBeSimplified()) arguments[2] = arguments[2].simplify(ctx.machine());
                    ret(ctx, arr.arrayAccess((int)(long)arguments[2].extractAs(Long.class)));
                    return;
                }
                if (inst.name.startsWith("get") && inst.desc.startsWith("(Ljava/lang/Object;J)") && arguments[2] instanceof UnsafeFieldReference ref) {
                    ret(ctx, arguments[1].getField(ref.fieldName()));
                    return;
                }
                if (inst.name.startsWith("put") && inst.desc.startsWith("(Ljava/lang/Object;J") && inst.desc.endsWith(")V") && arguments[1] instanceof KnownArray arr) {
                    // Arrays may use normal longs as indices
                    if (arguments[2].canBeSimplified()) arguments[2] = arguments[2].simplify(ctx.machine());
                    arr.arraySet((int)(long)arguments[2].extractAs(Long.class), arguments[4]);
                    ret(ctx, null);
                    return;
                }
                if (inst.name.startsWith("put") && inst.desc.startsWith("(Ljava/lang/Object;J") && inst.desc.endsWith(")V") && arguments[2] instanceof UnsafeFieldReference ref) {
                    arguments[1].setField(ref.fieldName(), arguments[3]);
                    ret(ctx, null);
                    return;
                }
                if (inst.name.startsWith("compareAndSet") && arguments[1] instanceof KnownArray arr) {
                    // Arrays may use normal longs as indices
                    if (arguments[2].canBeSimplified()) arguments[2] = arguments[2].simplify(ctx.machine());
                    arr.arraySet((int)(long)arguments[2].extractAs(Long.class), arguments[4]);
                    ret(ctx, new KnownInteger(true));
                    return;
                }
                if (inst.name.startsWith("compareAndSet") && arguments[2] instanceof UnsafeFieldReference ref) {
                    // No need to compare, this isn't threaded anyway
                    arguments[1].setField(ref.fieldName(), arguments[4]);
                    ret(ctx, new KnownInteger(true));
                    return;
                }
                if (inst.name.startsWith("compareAndExchange") && arguments[2] instanceof UnsafeFieldReference ref) {
                    // No need to compare, this isn't threaded anyway
                    var original = arguments[1].getField(ref.fieldName());
                    arguments[1].setField(ref.fieldName(), arguments[4]);
                    ret(ctx, original);
                    return;
                }
            }
            if (inst.owner.equals(_System)) {
                if (inst.name.equals("registerNatives")) {
                    ret(ctx, null);
                    return;
                }
                if (inst.name.equals("arrayCopy")) {
                    for (var i = 0; i <= arguments.length; i++) {
                        if (arguments[i].canBeSimplified()) arguments[i] = arguments[i].simplify(ctx.machine());
                    }
                    if (arguments[0] instanceof KnownArray src && arguments[2] instanceof KnownArray dst) {
                        System.arraycopy(src.data(), arguments[1].extractAs(Integer.class), dst.data(), arguments[3].extractAs(Integer.class), arguments[4].extractAs(Integer.class));
                        ret(ctx, null);
                        return;
                    }
                }
            }
            if (inst.owner.equals(_Runtime)) {
                if (inst.name.equals("availableProcessors")) {
                    // Do you think this vm supports multithreading??? Lmao
                    ret(ctx, new KnownInteger(1));
                    return;
                }
                if (inst.name.equals("freeMemory")) {
                    ret(ctx, new KnownLong(Runtime.getRuntime().freeMemory() / 2));
                    return;
                }
                if (inst.name.equals("totalMemory")) {
                    ret(ctx, new KnownLong(Runtime.getRuntime().totalMemory() / 2));
                    return;
                }
                if (inst.name.equals("maxMemory")) {
                    ret(ctx, new KnownLong(Runtime.getRuntime().maxMemory()));
                    return;
                }
                if (inst.name.equals("gc")) {
                    System.gc(); // I guess we'll trust that we do in fact need some gc
                    ret(ctx, null);
                    return;
                }
            }
            if (inst.name.equals("getClass") && inst.desc.equals("()Ljava/lang/Class;")) {
                var first = arguments[0];
                if (first.canBeSimplified()) first = first.simplify(ctx.machine());
                ret(ctx, new KnownClass(ctx.machine.getType(first)));
                return;
            }
            if (inst.owner.startsWith("[") && inst.name.equals("clone") && Util.first(arguments) instanceof KnownArray array) {
                ret(ctx, array.shallowCopy());
                return;
            }

            if (inst.getOpcode() != Opcodes.INVOKESTATIC) {
                // TODO figure out what to do with this
                if (arguments[0].canBeSimplified()) arguments[0] = arguments[0].simplify(ctx.machine());
            }

            if (Util.first(arguments) instanceof Lambda lambda && inst.getOpcode() != Opcodes.INVOKESPECIAL && inst.getOpcode() != Opcodes.INVOKESTATIC) {
                // This might break, it was only designed to deal with one specific type of lambda and idk if all lambda's act the same
                var method = lambda.method();
                var clazz = ctx.machine().getClass(method.getOwner());
                // Remove first arg
                var newArgs = new StackEntry[lambda.extraArguments().length+arguments.length-1];
                System.arraycopy(lambda.extraArguments(), 0, newArgs, 0, lambda.extraArguments().length);
                System.arraycopy(arguments, 1, newArgs, lambda.extraArguments().length, arguments.length - 1);
                ctx.machine.addMethodToStack(clazz, method.getName(), method.getDesc(), newArgs);
                return;
            }

            // I'm pretty sure we're supposed to run clinit at this point, but let's delay
            // it as much as possible
            try {
                var method = ctx.machine().resolveMethod(currentClass, inst, Util.first(arguments));
                invoke(ctx, currentClass, inst, arguments, method);
            } catch (VmException e) {
                // This method is part of Object and wouldn't be found otherwise
                if (inst.name.equals("hashCode") && inst.desc.equals("()I")) {
                    if (Util.first(arguments) instanceof KnownObject obj) {
                        ret(ctx, new KnownInteger(obj.i().hashCode())); // It's no problem to do this in the outer vm
                        return;
                    }
                    if (Util.first(arguments) instanceof KnownVmObject obj) {
                        ret(ctx, new KnownInteger(obj.hashCode())); // It's no problem to do this in the outer vm
                        return;
                    }
                    if (Util.first(arguments) instanceof KnownClass cl) {
                        ret(ctx, new KnownInteger(cl.type().hashCode())); // It's no problem to do this in the outer vm
                        return;
                    }
                }

                throw e;
            }
        }

        default void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments, @Nullable MethodRef methodRef) throws VmException {
            if (methodRef == null) {
                // Can't be resolved, return an unknown value
                ret(ctx, Type.getReturnType(inst.desc) == Type.VOID_TYPE ? null
                        : new UnknownValue("Can't resolve "+inst.owner+"#"+inst.name+inst.desc+" because "+Util.first(arguments)+" has no type"));
                return;
            }

            if (methodRef.className().equals(String) && arguments[0] instanceof KnownObject o && o.i() instanceof String str) {
                // String is quite strict with its fields, we can't reflect into them
                // This causes the jvm to be unable to execute many methods inside String
                if (methodRef.name().equals("isEmpty")) {
                    ret(ctx, new KnownInteger(str.isEmpty()));
                    return;
                }
                if (methodRef.name().equals("length")) {
                    ret(ctx, new KnownInteger(str.length()));
                    return;
                }
                if (methodRef.name().equals("hashCode")) {
                    ret(ctx, new KnownInteger(str.length()));
                    return;
                }
                if (methodRef.name().equals("charAt")) {
                    if (arguments[1].canBeSimplified()) arguments[1].simplify(ctx.machine());
                    if (arguments[1].isConcrete()) {
                        ret(ctx, new KnownInteger(str.charAt(arguments[1].extractAs(Integer.class))));
                        return;
                    }
                }
                if (methodRef.name().equals("indexOf") && methodRef.desc().equals("(II)v")) {
                    if (arguments[1].canBeSimplified()) arguments[1].simplify(ctx.machine());
                    if (arguments[2].canBeSimplified()) arguments[2].simplify(ctx.machine());
                    if (arguments[1].isConcrete() && arguments[2].isConcrete()) {
                        ret(ctx, new KnownInteger(str.indexOf(arguments[1].extractAs(Integer.class), arguments[2].extractAs(Integer.class))));
                        return;
                    }
                }
            }
            ctx.machine.addMethodToStack(methodRef, arguments);
        }

        default @NotNull StackEntry newObject(Context ctx, TypeInsnNode inst) throws VmException {
            var clazz = ctx.machine.getClass(inst.desc);
            return new KnownVmObject(clazz, new CowCapableMap<>());
        }

        default void handleUnknownInstruction(Context ctx, AbstractInsnNode instruction, int lineNumber)
                throws VmException {
            throw new NotImplementedException("Unimplemented instruction " + instruction.getOpcode());
        }

        default void handleUnknownJump(Context ctx, StackEntry compA, @Nullable StackEntry compB, int opcode, LabelNode target) throws VmException {
            throw new VmException("Jump on unknown value(s) ("+compA+", "+compB+")", null);
        }

        /**
         * Just a convenience method. Probably shouldn't override this one since it doesn't receive all returns
         */
        default void ret(Context ctx, StackEntry e) {
            // We're going to create a "method" and act like that one returned the value
            ctx.machine.methodStack.push(new MethodExecutor(ctx.machine(), new StackEntry[0], "Fake method"));
            ctx.machine.onMethodReturn(e);
        }
    }

    public record Context(VirtualMachine machine) {
    }

    public record MethodRef(@NotNull Clazz clazz, @NotNull MethodNode meth) {
        public String className() {
            return clazz.node.name;
        }

        public String name() {
            return meth.name;
        }

        public String desc() {
            return meth.desc;
        }
    };

    public static class Clazz {
        private final VirtualMachine loader;
        private final ClassNode node;
        private boolean hasInitted;
        private final Map<String, @NotNull StackEntry> staticFields = new HashMap<>();
        private final Map<String, @NotNull MethodNode> methodLookupCache = new HashMap<>();

        public Clazz(ClassNode node, VirtualMachine loader) {
            this.node = node;
            // Populate method lookup cache
            this.node.methods.forEach(method -> {
                methodLookupCache.put(method.name+method.desc, method);
            });
            this.loader = loader;
        }

        @ApiStatus.Internal
        @ApiStatus.Experimental
        public @NotNull VirtualMachine getLoader() {
            return this.loader;
        }

        public @NotNull StackEntry getStatic(String name) {
            var value = staticFields.get(name);
            if (value == null) {
                // We need to get the default value depending on the type of the field
                var field = AsmUtils.getFields(this)
                        .filter(f -> f.name.equals(name))
                        .filter(f -> AsmUtils.hasFlag(f, Opcodes.ACC_STATIC))
                        .findAny().orElse(null);
                if (field == null) {
                    return new UnknownValue("Don't know value of static field '"+name+"'");
                }
                return switch (field.desc) {
                    case "I", "Z", "S", "C", "B" -> new KnownInteger(0);
                    case "J" -> new KnownLong(0);
                    case "F" -> new KnownFloat(0);
                    case "D" -> new KnownDouble(0);
                    default -> KnownObject.NULL;
                };
            }
            return value;
        }

        public void setStatic(String name, @NotNull StackEntry v) {
            staticFields.put(name, v);
        }

        public @Nullable MethodNode getMethod(String name, String descriptor) {
            return methodLookupCache.get(name+descriptor);
        }

        @Override
        public String toString() {
            return node.name;
        }

        public ClassNode getNode() {
            return node;
        }
    }
}
