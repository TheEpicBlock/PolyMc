package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.impl.generator.asm.ClientInitializerAnalyzer.EntityModelLayer;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Clazz;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.StaticFieldValue;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class EntityRendererAnalyzer {
    public static final SharedValuesKey<EntityRendererAnalyzer> KEY = new SharedValuesKey<>(EntityRendererAnalyzer::new, null);

    private final ClientInitializerAnalyzer initializerInfo;

    private final StackEntry cachedMinecraftClient;

    private final static String RotationAxis = AsmUtils.mapClass("net.minecraft.class_7833");
    private final static AsmUtils.MappedFunction EntityModelLoader$getModelPart = AsmUtils.map("net.minecraft.class_5599", "method_32072", "(Lnet/minecraft/class_5601;)Lnet/minecraft/class_630;");
    private final static AsmUtils.MappedFunction ModelPart$Cuboid$renderCuboid = AsmUtils.map("net.minecraft.class_630$class_628", "method_32089", "(Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_4588;IIFFFF)V");
    private final static AsmUtils.MappedFunction MobEntityRenderer$renderLeash = AsmUtils.map("net.minecraft.class_927", "method_4073", "(Lnet/minecraft/class_1308;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1297;)V");
    private final static AsmUtils.MappedFunction TextRenderer$drawInternal = AsmUtils.map("net.minecraft.class_327", "method_1723", "(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)I");
    private final static AsmUtils.MappedFunction EntityRenderer$renderLabelIfPresent = AsmUtils.map("net.minecraft.class_897", "method_3926", "(Lnet/minecraft/class_1297;Lnet/minecraft/class_2561;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
    private final static String Entity$type = AsmUtils.mapField(Entity.class, "field_5961", "Lnet/minecraft/class_1299;");
    private final static String Entity$dimensions = AsmUtils.mapField(Entity.class, "field_18065", "Lnet/minecraft/class_4048;");
    private final static AsmUtils.MappedFunction VertexConsumerProvider$getBuffer = AsmUtils.map("net.minecraft.class_4597", "getBuffer", "(Lnet/minecraft/class_1921;)Lnet/minecraft/class_4588;");
    private final static String MatrixStack = AsmUtils.mapClass("net.minecraft.class_4587");

    // Used to detect loops
    private final static AsmUtils.MappedFunction Iterator$hasNext = AsmUtils.map("java.util.Iterator", "hasNext", "()Z");

    // Needs to be special-cased because I'm not instantiating a darn *MinecraftClient* instance in the vm
    private final static AsmUtils.MappedFunction MinecraftClient$getInstance = AsmUtils.map("net.minecraft.class_310", "method_1551", "()Lnet/minecraft/class_310;");
    private final static AsmUtils.MappedFunction MinecraftClient$hasOutline = AsmUtils.map("net.minecraft.class_310", "method_27022", "(Lnet/minecraft/class_1297;)Z");

    // We can consider these methods to just be properties of the entity
    // They're not executed in order to prevent jumps on unknown values
    private final static AsmUtils.MappedFunction LivingEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_922", "method_4055", "(Lnet/minecraft/class_1309;)Z");
    private final static AsmUtils.MappedFunction MobEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_927", "method_4071", "(Lnet/minecraft/class_1308;)Z");

    /**
     * Vm for invoking the factory
     */
    private final VirtualMachine factoryVm = new VirtualMachine(new ClientClassLoader(), new VmConfig() {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, Clazz owner, String fieldName) throws VmException {
            return new StaticFieldValue(owner.getNode().name, fieldName); // Evaluate static fields lazily
        }

        @Override
        public void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            if (cmpFunc(inst, EntityModelLoader$getModelPart)) {
                var modelLayer = arguments[1].simplify(ctx.machine()).extractAs(EntityModelLayer.class);
                var texturedModelDataProvider = initializerInfo.getEntityModelLayer(modelLayer);
                if (texturedModelDataProvider == null) {
                    PolyMc.LOGGER.warn("Mod tried to access non-existent model layer " + modelLayer);
                    ret(ctx, KnownObject.NULL);
                    return;
                }

                // Create a new state to run these in
                var state = factoryVm.switchStack(null);
                var texturedModelData = factoryVm.runLambda(texturedModelDataProvider, new StackEntry[0]);
                var texturedModelData$createModel = AsmUtils.map("net.minecraft.class_5607", "method_32109", "()Lnet/minecraft/class_630;");
                var createModelFunc = factoryVm.resolveMethod(null, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, texturedModelData$createModel.clazz(), texturedModelData$createModel.method(), texturedModelData$createModel.desc(), false), texturedModelData);
                if (createModelFunc == null || createModelFunc.isAbstract()) throw new RuntimeException("PolyMc: Couldn't find model creation function");
                factoryVm.addMethodToStack(createModelFunc, new StackEntry[]{ texturedModelData });
                var ret = factoryVm.runToCompletion();
                factoryVm.switchStack(state); // Restore state
                ret(ctx, ret);
                return;
            }

            VmConfig.super.invoke(ctx, currentClass, inst, arguments);
        }

        @Override
        public StackEntry onVmError(String method, boolean returnsVoid, VmException e) throws VmException {
//            PolyMc.LOGGER.error("Couldn't run "+method+": "+e.createFancyErrorMessage());
            if (returnsVoid) {
                return null;
            }
            return new UnknownValue("Error executing "+method+": "+e.createFancyErrorMessage());
        }
    });

    public EntityRendererAnalyzer(PolyRegistry registry) {
        this.initializerInfo = registry.getSharedValues(ClientInitializerAnalyzer.KEY);
        try {
            this.cachedMinecraftClient = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_310", "minecraftClient");
        } catch (VmException e) {
            throw new RuntimeException(e);
        }
    }

    private final HashSet<String> branchCauses = new HashSet<>();
    private final HashSet<String> errorCauses = new HashSet<>();
    public int amountOfVmsTotal = 1;
    public int amountOfVms = 1;
    public StopWatch time;

    public ExecutionGraphNode analyze(EntityType<?> entity) throws VmException {
        var rootNode = new ExecutionGraphNode();
        var rendererFactory = initializerInfo.getEntityRenderer(entity);

        if (rendererFactory == null) { return null; }

        branchCauses.clear();
        errorCauses.clear();
        time = new StopWatch();
        time.start();
        // EntityRendererFactory.Context
        var ctx = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_5617$class_5618", "context");

        var renderer = factoryVm.runLambda(rendererFactory, new StackEntry[]{ ctx });

        // We need to create a fake instruction that's calling the render method so we can resolve it
        var entityRenderer$render = AsmUtils.map("net.minecraft.class_897", "method_3936", "(Lnet/minecraft/class_1297;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
        var inst = new MethodInsnNode(Opcodes.INVOKEINTERFACE, entityRenderer$render.clazz(), entityRenderer$render.method(), entityRenderer$render.desc(), false);
        var resolvedEntityRenderer$render = factoryVm.resolveMethod(null, inst, renderer);
        if (resolvedEntityRenderer$render == null || resolvedEntityRenderer$render.isAbstract()) {
            PolyMc.LOGGER.error("Failed to find render method for "+entity.getTranslationKey());
            return null;
        }

        var entityClass = InternalEntityHelpers.getEntityClass(entity);
        entityClass = entityClass == null ? Entity.class : entityClass;
        var fakeEntity = AsmUtils.mockVmObject(factoryVm, entityClass.getName().replace(".", "/"), "entity");
        fakeEntity.setField(Entity$type, StackEntry.known(entity));
        // The dimension technically could've changed in the constructor, this doesn't handle that
        fakeEntity.setField(Entity$dimensions, StackEntry.known(entity.getDimensions()));
        var matrixStack = createMatrixStack();

        var rendererVm = new VirtualMachine(new ClientClassLoader(), new RendererAnalyzerVmConfig(rootNode, null, this));
        var yaw = new MockedObject(new MockedObject.Root("yaw"), rendererVm.getType(Type.FLOAT_TYPE));
        var tickDelta = new MockedObject(new MockedObject.Root("tickDelta"), rendererVm.getType(Type.FLOAT_TYPE));
        var light = new MockedObject(new MockedObject.Root("light"), rendererVm.getType(Type.INT_TYPE));
        rendererVm.addMethodToStack(resolvedEntityRenderer$render, new StackEntry[] { renderer, fakeEntity, yaw, tickDelta, matrixStack, KnownObject.NULL, light });
        rendererVm.runToCompletion();

        PolyMc.LOGGER.info("Finished analysing "+entity.getTranslationKey());
        PolyMc.LOGGER.info("Branch causes");
        branchCauses.forEach(cause -> PolyMc.LOGGER.info(" - "+cause));
        PolyMc.LOGGER.info("Error causes");
        errorCauses.forEach(cause -> PolyMc.LOGGER.info(" - "+cause));

        return rootNode;
    }

    public StackEntry createMatrixStack() throws VmException {
        var matrixStackClass = factoryVm.getClass(MatrixStack);
        var matrixStack = new KnownVmObject(matrixStackClass);
        // Create new state for the vm, so we can generate a matrixStack without ruining other things
        var vmState = factoryVm.switchStack(null);
        factoryVm.addMethodToStack(factoryVm.getClass(MatrixStack), "<init>", "()V", new StackEntry[] { matrixStack });
        factoryVm.runToCompletion();
        factoryVm.switchStack(vmState);

        return matrixStack;
    }

    public static boolean cmpFunc(@NotNull VirtualMachine.MethodRef method, @NotNull AsmUtils.MappedFunction func) {
        return (method.className().equals(func.clazz()) &&
                method.name().equals(func.method()) &&
                method.desc().equals(func.desc()));
    }

    /**
     * Will match any function that overrides a specified function.
     */
    public static boolean cmpFuncOrOverrides(@NotNull VirtualMachine.MethodRef method, @NotNull AsmUtils.MappedFunction func) {
        return (// TODO, check if it actually overrides
                method.meth().name.equals(func.method()) &&
                method.meth().desc.equals(func.desc()));
    }

    public static boolean cmpFunc(@NotNull MethodInsnNode inst, @NotNull AsmUtils.MappedFunction func) {
        return (inst.owner.equals(func.clazz()) &&
                inst.name.equals(func.method()) &&
                inst.desc.equals(func.desc()));
    }

    public record RendererAnalyzerVmConfig(ExecutionGraphNode node, @Nullable RendererAnalyzerVmConfig parent, EntityRendererAnalyzer root) implements VmConfig {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, Clazz owner, String fieldName) throws VmException {
            if (!owner.hasInitted() && !owner.name().equals(RotationAxis)) {
                var fromEnvironment = AsmUtils.tryGetStaticFieldFromEnvironment(ctx, owner.name(), fieldName);
                if (fromEnvironment != null) return fromEnvironment;
            }
            return new StaticFieldValue(owner.name(), fieldName);
        }

        private static final Map<AsmUtils.MappedFunction, SpecialMethod> SPECIAL_METHODS = new HashMap<>();

        @FunctionalInterface
        private interface SpecialMethod {
            StackEntry apply(StackEntry[] arguments, RendererAnalyzerVmConfig config) throws VmException;
        }

        static {
            SPECIAL_METHODS.put(ModelPart$Cuboid$renderCuboid, (arguments, config) -> {
                var cuboid = arguments[0].copy();
                var matrix = arguments[1].getField("positionMatrix").copy(); // TODO mappings
                // TODO check which texture is being used (should be somewhere in the vertex buffer?)
                config.node.addCall(new ExecutionGraphNode.RenderCall(cuboid, matrix));
                return null;
            });
            SPECIAL_METHODS.put(MobEntityRenderer$renderLeash, (arguments, config) -> {
                // TODO should probably track these calls
                return null;
            });
            SPECIAL_METHODS.put(TextRenderer$drawInternal, (arguments, config) -> {
                // TODO should probably track these calls
                return null;
            });
            SPECIAL_METHODS.put(EntityRenderer$renderLabelIfPresent, (arguments, config) -> {
                // TODO should probably track these calls
                return null;
            });
            // Other special-cased functions
            SPECIAL_METHODS.put(MinecraftClient$getInstance, (arguments, config) -> {
                return config.root.cachedMinecraftClient;
            });
            SPECIAL_METHODS.put(MinecraftClient$hasOutline, (arguments, config) -> {
                return new KnownInteger(false); // Let's… not deal with that now
            });
            SPECIAL_METHODS.put(LivingEntityRenderer$hasLabel, (arguments, config) -> {
                return new MockedObject(new MockedObject.Root("hasLabel"), config.root.factoryVm.getType(Type.BOOLEAN_TYPE));
            });
            SPECIAL_METHODS.put(MobEntityRenderer$hasLabel, (arguments, config) -> {
                return new MockedObject(new MockedObject.Root("hasLabel"), config.root.factoryVm.getType(Type.BOOLEAN_TYPE));
            });
        }

        @Override
        public void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments, @Nullable VirtualMachine.MethodRef method) throws VmException {
            if (method == null) {
                // Skip all the comparisons, it's null anyway
                VmConfig.super.invoke(ctx, currentClass, inst, arguments, method);
                return;
            }

            if (cmpFuncOrOverrides(method, Iterator$hasNext) && !arguments[0].isConcrete()) {
                // If we don't do this, infinite amounts of forks will be created.
                // One for each potential iteration of the loop
                throw new VmException("Attempt to loop on unknown value "+arguments[0], null);
            }

            var specialMethod = SPECIAL_METHODS.get(new AsmUtils.MappedFunction(inst.owner, inst.name, inst.desc));
            if (specialMethod != null) {
                ret(ctx, specialMethod.apply(arguments, this));
                return;
            }

            VmConfig.super.invoke(ctx, currentClass, inst, arguments, method);
        }

        @Override
        public void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            // The vertex consumer is actually KnownObject(null), so it produces an npe
            // That's why this function is placed here, before the method is actually resolved to bytecode
            if (cmpFunc(inst, VertexConsumerProvider$getBuffer)) {
                // TODO, needs to be tracked further
                ret(ctx, new UnknownValue("VertexBuffer"));
                return;
            }

            VmConfig.super.invoke(ctx, currentClass, inst, arguments);
        }

        @Override
        public void handleUnknownJump(Context ctx, StackEntry compA, @Nullable StackEntry compB, int opcode, LabelNode target) throws VmException {
            if (ctx.machine().isClinit()) {
                // Cloning a VM whilst it's doing clinit just leads to problems. We're likely not interested in this anyway
                VmConfig.super.handleUnknownJump(ctx, compA, compB, opcode, target);
                return;
            }

            // Let's check our parents to see if we already jumped on this variable.
            // If so, we've already decided on the result of this comparison and we can just take that result
            var config = this;
            var previous = this;

            while (config.parent != null) {
                config = config.parent;

                var cont = config.node.getContinuation();
                assert cont != null; // This node is a parent, it should have children

                // Normalize the opcodes
                var thisOpcode = opcode;
                var invert = false;
                if (thisOpcode == Opcodes.IFNE) {
                    thisOpcode = Opcodes.IFEQ;
                    invert = true;
                } // TODO there are more cases where this is relevant

                var thatOpcode = cont.opcode();
                if (thatOpcode == Opcodes.IFNE) {
                    thatOpcode = Opcodes.IFEQ;
                    invert = !invert;
                } // TODO there are more cases where this is relevant

                if (Objects.equals(cont.compA(), compA) &&
                        Objects.equals(cont.compB(), compB) &&
                        Objects.equals(thatOpcode, thisOpcode)) {
                    var isTrue = previous.node() == cont.continuationIfTrue();
                    if (invert) isTrue = !isTrue;

                    if (isTrue) {
                        // True, so we should take the jump
                        ctx.machine().inspectRunningMethod().overrideNextInsn(target);
                    } else {
                        // False, so we should advance to the next instruction
                        var currentMethod = ctx.machine().inspectRunningMethod();
                        currentMethod.overrideNextInsn(currentMethod.inspectCurrentInsn().getNext());
                    }

                    return;
                }

                previous = config;
            }

            // We haven't decided on a value yet, so we must take both paths
            // we're going to clone the vm to create a continuation
            // The clone will take the jump, and we won't

            root.branchCauses.add(ctx.machine().inspectRunningMethod().getName() + "-" + ctx.machine().inspectRunningMethod().getLineNumber());

            var continuationNoJump = new ExecutionGraphNode();
            var continuationJump = new ExecutionGraphNode();
            var ifStmnt = new ExecutionGraphNode.IfStatement(compA, compB, opcode, continuationNoJump, continuationJump);
            this.node.setContinuation(ifStmnt);

            // SAFETY: none of the objects in `vmNoJump` may be mutated until `vmJump` is disposed of
            var vmNoJump = ctx.machine();
            var vmJump = ctx.machine().copyTmp();

            vmNoJump.changeConfig(new RendererAnalyzerVmConfig(continuationNoJump, this, root));
            vmJump.changeConfig(new RendererAnalyzerVmConfig(continuationJump, this, root));

            var noJumpMeth = vmNoJump.inspectRunningMethod();
            noJumpMeth.overrideNextInsn(noJumpMeth.inspectCurrentInsn().getNext());
            var jumpMeth = vmJump.inspectRunningMethod();
            jumpMeth.overrideNextInsn(target);

            root.amountOfVmsTotal++;
            if (root.amountOfVmsTotal % 20000 == 0) {
                PolyMc.LOGGER.info(root.amountOfVmsTotal+": "+root.time.formatTime());
                root.time.reset();
                root.time.start();
            }
            root.amountOfVms++;
            // This vm was cloned, and needs to restarted
            vmJump.runToCompletion();
            continuationJump.simplify();
            root.amountOfVms--;
            // The other vm will continue after this call
        }

        @Override
        public StackEntry onVmError(String method, boolean returnsVoid, VmException e) throws VmException {
            root.errorCauses.add(method);
            if (returnsVoid) {
                return null;
            }
            return new UnknownValue("Error executing " + method + ": " + e.createFancyErrorMessage());
        }
    }
}
