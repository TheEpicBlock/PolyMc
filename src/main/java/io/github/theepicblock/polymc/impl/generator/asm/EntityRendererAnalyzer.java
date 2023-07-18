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
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.UnaryArbitraryOp;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class EntityRendererAnalyzer {
    public static final SharedValuesKey<EntityRendererAnalyzer> KEY = new SharedValuesKey<EntityRendererAnalyzer>(EntityRendererAnalyzer::new, null);

    private final ClientInitializerAnalyzer initializerInfo;

    private final static AsmUtils.MappedFunction EntityModelLoader$getModelPart = AsmUtils.map("net.minecraft.class_5599", "method_32072", "(Lnet/minecraft/class_5601;)Lnet/minecraft/class_630;");
    private final static AsmUtils.MappedFunction ModelPart$Cuboid$renderCuboid = AsmUtils.map("net.minecraft.class_630.class_628", "method_32089", "(Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_4588;IIFFFF)V");

    // Used to detect loops
    private final static AsmUtils.MappedFunction Iterator$hasNext = AsmUtils.map("java.util.Iterator", "hasNext", "()Z");

    // We can consider these methods to just be properties of the entity
    // They're not executed in order to prevent jumps on unknown values
    private final static AsmUtils.MappedFunction Entity$isFrozen = AsmUtils.map("net.minecraft.class_1297", "method_32314", "()Z");
    private final static AsmUtils.MappedFunction Entity$getFlag = AsmUtils.map("net.minecraft.class_1297", "method_5795", "(I)Z");
    private final static AsmUtils.MappedFunction Nameable$getName = AsmUtils.map("net.minecraft.class_1275", "method_5477", "()Lnet/minecraft/class_2561;");
    private final static AsmUtils.MappedFunction LivingEntity$isUsingRiptide = AsmUtils.map("net.minecraft.class_1309", "method_6123", "()Z");
    private final static AsmUtils.MappedFunction LivingEntity$getHandSwingProgress = AsmUtils.map("net.minecraft.class_1309", "method_6055", "(F)F");
    private final static AsmUtils.MappedFunction LivingEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_922", "method_4055", "(Lnet/minecraft/class_1309;)Z");
    private final static AsmUtils.MappedFunction MobEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_927", "method_4071", "(Lnet/minecraft/class_1308;)Z");

    // These methods are functional and have no side effects.
    // They're executed lazily in order to prevent jumps on unknown values
    private final static AsmUtils.MappedFunction LivingEntityRenderer$shouldFlipUpsideDown = AsmUtils.map("net.minecraft.class_922", "method_38563", "(Lnet/minecraft/class_1309;)Z");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesI = AsmUtils.map("net.minecraft.class_3532", "method_15392", "(I)I");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesF = AsmUtils.map("net.minecraft.class_3532", "method_15393", "(F)F");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesD = AsmUtils.map("net.minecraft.class_3532", "method_15338", "(D)D");
    private final static AsmUtils.MappedFunction Entity$removeClickEvents = AsmUtils.map("net.minecraft.class_1297", "method_5856", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_2561;");

    /**
     * Vm for invoking the factory
     */
    private final VirtualMachine factoryVm = new VirtualMachine(new ClientClassLoader(), (VmConfig) new VmConfig() {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
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
                if (createModelFunc == null) throw new RuntimeException("PolyMc: Couldn't find model creation function");
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
            PolyMc.LOGGER.error("Couldn't run "+method+": "+e.createFancyErrorMessage());
            if (returnsVoid) {
                return null;
            }
            return new UnknownValue("Error executing "+method+": "+e.createFancyErrorMessage());
        }
    });

    public EntityRendererAnalyzer(PolyRegistry registry) {
        this.initializerInfo = registry.getSharedValues(ClientInitializerAnalyzer.KEY);
    }

    public ExecutionGraphNode analyze(EntityType<?> entity) throws VmException {
        var rootNode = new ExecutionGraphNode();
        var rendererFactory = initializerInfo.getEntityRenderer(entity);

        if (rendererFactory == null) { return null; }

        // EntityRendererFactory.Context
        var ctx = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_5617$class_5618");

        var renderer = factoryVm.runLambda(rendererFactory, new StackEntry[]{ ctx });

        System.out.println("Renderer for "+entity.getTranslationKey()+": "+renderer);

        // We need to create a fake instruction that's calling the render method so we can resolve it
        var entityRenderer$render = AsmUtils.map("net.minecraft.class_897", "method_3936", "(Lnet/minecraft/class_1297;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
        var inst = new MethodInsnNode(Opcodes.INVOKEINTERFACE, entityRenderer$render.clazz(), entityRenderer$render.method(), entityRenderer$render.desc(), false);
        var resolvedEntityRenderer$render = factoryVm.resolveMethod(null, inst, renderer);
        if (resolvedEntityRenderer$render == null) {
            PolyMc.LOGGER.error("Failed to find render method for "+entity.getTranslationKey());
            return null;
        }

        var entityClass = InternalEntityHelpers.getEntityClass(entity);
        entityClass = entityClass == null ? Entity.class : entityClass;
        var fakeEntity = AsmUtils.mockVmObject(factoryVm, entityClass.getName().replace(".", "/"));
        var matrixStack = createMatrixStack();

        // TODO all of these variables should be dynamic/symbolic
        var rendererVm = new VirtualMachine(new ClientClassLoader(), new RendererAnalyzerVmConfig(rootNode));
        rendererVm.addMethodToStack(resolvedEntityRenderer$render, new StackEntry[] { renderer, fakeEntity, new KnownFloat(0.0f), new KnownFloat(0.0f), matrixStack, KnownObject.NULL, new KnownInteger(0) });
        rendererVm.runToCompletion();
        return rootNode;
    }

    public StackEntry createMatrixStack() throws VmException {
        var fmapper = FabricLoader.getInstance().getMappingResolver();
        var matrixStackInt = "net.minecraft.class_4587";
        var matrixStackRun = fmapper.mapClassName("intermediary", matrixStackInt).replace(".", "/");
        var matrixStack = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_4587");
        // Create new state for the vm, so we can generate a matrixStack without ruining other things
        var vmState = factoryVm.switchStack(null);
        factoryVm.addMethodToStack(factoryVm.getClass(matrixStackRun), "<init>", "()V", new StackEntry[] { matrixStack });
        factoryVm.runToCompletion();
        factoryVm.switchStack(vmState);

        return matrixStack;
    }

    public static boolean cmpFunc(VirtualMachine.MethodRef method, AsmUtils.MappedFunction func) {
        return (method.clazz().getNode().name.equals(func.clazz()) &&
                method.meth().name.equals(func.method()) &&
                method.meth().desc.equals(func.desc()));
    }

    public static boolean cmpInterfaceFunc(VirtualMachine.MethodRef method, AsmUtils.MappedFunction func) {
        return (AsmUtils.getInheritanceChain(method.clazz()).stream().anyMatch(node -> func.clazz().equals(node.name)) &&
                method.meth().name.equals(func.method()) &&
                method.meth().desc.equals(func.desc()));
    }

    public static boolean cmpFunc(MethodInsnNode inst, AsmUtils.MappedFunction func) {
        return (inst.owner.equals(func.clazz()) &&
                inst.name.equals(func.method()) &&
                inst.desc.equals(func.desc()));
    }

    public record RendererAnalyzerVmConfig(ExecutionGraphNode node) implements VmConfig {

        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
        }

        @Override
        public void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments, VirtualMachine.MethodRef method) throws VmException {
            if (cmpFunc(method, ModelPart$Cuboid$renderCuboid)) {
                var cuboid = arguments[0];
                var matrix = arguments[1].getField("positionMatrix"); // TODO mappings
                node.addCall(new ExecutionGraphNode.RenderCall(cuboid, matrix));
                ret(ctx, null); // Function is void
                return;
            }

            // Other special-cased functions
            if (cmpFunc(method, Iterator$hasNext) && !arguments[0].isConcrete()) {
                // If we don't do this, infinite amounts of parallel universes will be created.
                // One for each potential iteration of the loop
                throw new VmException("Attempt to loop on unknown value "+arguments[0], null);
            }
            if (cmpFunc(method, Entity$isFrozen)) {
                ret(ctx, new UnknownValue("isFrozen"));
                return;
            }
            if (cmpFunc(method, LivingEntity$isUsingRiptide)) {
                ret(ctx, new UnknownValue("isUsingRiptide"));
                return;
            }
            if (cmpFunc(method, Entity$getFlag)) {
                ret(ctx, new UnknownValue("getFlag"));
                return;
            }
            if (cmpInterfaceFunc(method, Nameable$getName)) {
                ret(ctx, new UnknownValue("getName"));
                return;
            }
            if (cmpFunc(method, LivingEntity$getHandSwingProgress)) {
                ret(ctx, new UnknownValue("getFlag"));
                return;
            }
            if (cmpFunc(method, LivingEntityRenderer$hasLabel)) {
                ret(ctx, new UnknownValue("hasLabel"));
                return;
            }
            if (cmpFunc(method, MobEntityRenderer$hasLabel)) {
                ret(ctx, new UnknownValue("hasLabel"));
                return;
            }
            if (cmpFunc(method, LivingEntityRenderer$shouldFlipUpsideDown)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> new KnownInteger(1))); // TODO
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesI)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(Integer.class)))));
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesF)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(Float.class)))));
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesD)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(Double.class)))));
                return;
            }
            if (cmpFunc(method, Entity$removeClickEvents)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> stackEntry)); // TODO
                return;
            }

            VmConfig.super.invoke(ctx, currentClass, inst, arguments, method);
        }

        @Override
        public void handleUnknownJump(Context ctx, StackEntry compA, @Nullable StackEntry compB, int opcode, LabelNode target) throws VmException {
            // We're going to clone the vm to create a parallel universe / continuation
            // The clone will take the jump, and we won't

            var continuationNoJump = new ExecutionGraphNode();
            var continuationJump = new ExecutionGraphNode();
            var ifStmnt = new ExecutionGraphNode.IfStatement(compA, compB, opcode, continuationNoJump, continuationJump);
            this.node.setContinuation(ifStmnt);

            var vmNoJump = ctx.machine();
            var vmJump = ctx.machine().copy();

            vmNoJump.changeConfig(new RendererAnalyzerVmConfig(continuationNoJump));
            vmJump.changeConfig(new RendererAnalyzerVmConfig(continuationJump));

            var noJumpMeth = vmNoJump.inspectRunningMethod();
            noJumpMeth.overrideNextInsn(noJumpMeth.inspectCurrentInsn().getNext());
            var jumpMeth = vmJump.inspectRunningMethod();
            jumpMeth.overrideNextInsn(target);

            // This vm was cloned, and needs to restarted
            vmJump.runToCompletion();
            // The other vm will continue after this call
        }

        @Override
        public StackEntry onVmError(String method, boolean returnsVoid, VmException e) throws VmException {
//            PolyMc.LOGGER.error("Couldn't run "+method+": "+e.createFancyErrorMessage());
            if (returnsVoid) {
                return null;
            }
            return new UnknownValue("Error executing "+method+": "+e.createFancyErrorMessage());
        }
    }
}
