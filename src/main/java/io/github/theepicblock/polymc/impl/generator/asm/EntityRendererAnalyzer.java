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
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.BinaryArbitraryOp;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.UnaryArbitraryOp;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class EntityRendererAnalyzer {
    public static final SharedValuesKey<EntityRendererAnalyzer> KEY = new SharedValuesKey<>(EntityRendererAnalyzer::new, null);

    private final ClientInitializerAnalyzer initializerInfo;

    private final KnownVmObject cachedMinecraftClient;
    private final static AsmUtils.MappedFunction EntityModelLoader$getModelPart = AsmUtils.map("net.minecraft.class_5599", "method_32072", "(Lnet/minecraft/class_5601;)Lnet/minecraft/class_630;");
    private final static AsmUtils.MappedFunction ModelPart$Cuboid$renderCuboid = AsmUtils.map("net.minecraft.class_630$class_628", "method_32089", "(Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_4588;IIFFFF)V");
    private final static AsmUtils.MappedFunction MobEntityRenderer$renderLeash = AsmUtils.map("net.minecraft.class_927", "method_4073", "(Lnet/minecraft/class_1308;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1297;)V");
    private final static AsmUtils.MappedFunction TextRenderer$drawInternal = AsmUtils.map("net.minecraft.class_327", "method_1723", "(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)I");
    private final static AsmUtils.MappedFunction EntityRenderer$renderLabelIfPresent = AsmUtils.map("net.minecraft.class_897", "method_3926", "(Lnet/minecraft/class_1297;Lnet/minecraft/class_2561;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
    private final static String Entity$type = AsmUtils.mapField(Entity.class, "field_5961", "Lnet/minecraft/class_1299;");
    private final static String Entity$dimensions = AsmUtils.mapField(Entity.class, "field_18065", "Lnet/minecraft/class_4048;");
    private final static AsmUtils.MappedFunction VertexConsumerProvider$getBuffer = AsmUtils.map("net.minecraft.class_4597", "getBuffer", "(Lnet/minecraft/class_1921;)Lnet/minecraft/class_4588;");

    // Used to detect loops
    private final static AsmUtils.MappedFunction Iterator$hasNext = AsmUtils.map("java.util.Iterator", "hasNext", "()Z");

    // Needs to be special-cased because I'm not instantiating a darn *MinecraftClient* instance in the vm
    private final static AsmUtils.MappedFunction MinecraftClient$getInstance = AsmUtils.map("net.minecraft.class_310", "method_1551", "()Lnet/minecraft/class_310;");
    private final static AsmUtils.MappedFunction MinecraftClient$hasOutline = AsmUtils.map("net.minecraft.class_310", "method_27022", "(Lnet/minecraft/class_1297;)Z");

    // We can consider these methods to just be properties of the entity
    // They're not executed in order to prevent jumps on unknown values
    private final static AsmUtils.MappedFunction Entity$isFrozen = AsmUtils.map("net.minecraft.class_1297", "method_32314", "()Z");
    private final static AsmUtils.MappedFunction Entity$getFlag = AsmUtils.map("net.minecraft.class_1297", "method_5795", "(I)Z");
    private final static AsmUtils.MappedFunction Nameable$getName = AsmUtils.map("net.minecraft.class_1275", "method_5477", "()Lnet/minecraft/class_2561;");
    private final static AsmUtils.MappedFunction Nameable$getDisplayName = AsmUtils.map("net.minecraft.class_1275", "method_5476", "()Lnet/minecraft/class_2561;");
    private final static AsmUtils.MappedFunction Nameable$getCustomName = AsmUtils.map("net.minecraft.class_1275", "method_5797", "()Lnet/minecraft/class_2561;");
    private final static AsmUtils.MappedFunction LivingEntity$isUsingRiptide = AsmUtils.map("net.minecraft.class_1309", "method_6123", "()Z");
    private final static AsmUtils.MappedFunction LivingEntity$getHandSwingProgress = AsmUtils.map("net.minecraft.class_1309", "method_6055", "(F)F");
    private final static AsmUtils.MappedFunction LivingEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_922", "method_4055", "(Lnet/minecraft/class_1309;)Z");
    private final static AsmUtils.MappedFunction MobEntityRenderer$hasLabel = AsmUtils.map("net.minecraft.class_927", "method_4071", "(Lnet/minecraft/class_1308;)Z");
    private final static AsmUtils.MappedFunction DataTracker$getEntry = AsmUtils.map("net.minecraft.class_2945", "method_12783", "(Lnet/minecraft/class_2940;)Lnet/minecraft/class_2945$class_2946;");

    // These methods are functional and have no side effects.
    // They're executed lazily in order to prevent jumps on unknown values
    private final static AsmUtils.MappedFunction LivingEntityRenderer$shouldFlipUpsideDown = AsmUtils.map("net.minecraft.class_922", "method_38563", "(Lnet/minecraft/class_1309;)Z");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesI = AsmUtils.map("net.minecraft.class_3532", "method_15392", "(I)I");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesF = AsmUtils.map("net.minecraft.class_3532", "method_15393", "(F)F");
    private final static AsmUtils.MappedFunction MathHelper$wrapDegreesD = AsmUtils.map("net.minecraft.class_3532", "method_15338", "(D)D");
    private final static AsmUtils.MappedFunction Entity$removeClickEvents = AsmUtils.map("net.minecraft.class_1297", "method_5856", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_2561;");
    private final static AsmUtils.MappedFunction Entity$isInPose = AsmUtils.map("net.minecraft.class_1297", "method_41328", "(Lnet/minecraft/class_4050;)Z");
    private final static AsmUtils.MappedFunction Entity$visitFormatted = AsmUtils.map("net.minecraft.class_1297", "method_41328", "(Lnet/minecraft/class_4050;)Z");

    /**
     * Vm for invoking the factory
     */
    private final VirtualMachine factoryVm = new VirtualMachine(new ClientClassLoader(), new VmConfig() {
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
        try {
            this.cachedMinecraftClient = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_310");
        } catch (VmException e) {
            throw new RuntimeException(e);
        }
    }

    public ExecutionGraphNode analyze(EntityType<?> entity) throws VmException {
//        if (true) return null;
        var rootNode = new ExecutionGraphNode();
        var rendererFactory = initializerInfo.getEntityRenderer(entity);

        if (rendererFactory == null) { return null; }

        // EntityRendererFactory.Context
        var ctx = AsmUtils.mockVmObjectRemap(factoryVm, "net.minecraft.class_5617$class_5618");

        var renderer = factoryVm.runLambda(rendererFactory, new StackEntry[]{ ctx });

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
        fakeEntity.setField(Entity$type, StackEntry.known(entity));
        // The dimension technically could've changed in the constructor, this doesn't handle that
        fakeEntity.setField(Entity$dimensions, StackEntry.known(entity.getDimensions()));
        var matrixStack = createMatrixStack();

        var rendererVm = new VirtualMachine(new ClientClassLoader(), new RendererAnalyzerVmConfig(rootNode, this));
        rendererVm.addMethodToStack(resolvedEntityRenderer$render, new StackEntry[] { renderer, fakeEntity, new UnknownValue("yaw"), new UnknownValue("tickDelta"), matrixStack, KnownObject.NULL, new UnknownValue("light") });
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

    public record RendererAnalyzerVmConfig(ExecutionGraphNode node, EntityRendererAnalyzer root) implements VmConfig {
        @Override
        public void invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments, @Nullable VirtualMachine.MethodRef method) throws VmException {
            if (method == null) {
                // Skip all the comparisons, it's null anyway
                VmConfig.super.invoke(ctx, currentClass, inst, arguments, method);
                return;
            }

            if (cmpFunc(method, ModelPart$Cuboid$renderCuboid)) {
                var cuboid = arguments[0];
                var matrix = arguments[1].getField("positionMatrix"); // TODO mappings
                node.addCall(new ExecutionGraphNode.RenderCall(cuboid, matrix));
                ret(ctx, null); // Function is void
                return;
            }
            if (cmpFunc(method, MobEntityRenderer$renderLeash)) {
                // TODO should probably track these calls
                ret(ctx, null); // Function is void
                return;
            }
            if (cmpFunc(method, TextRenderer$drawInternal)) {
                // TODO should probably track these calls
                ret(ctx, new UnknownValue("Width of the draw call or smth"));
                return;
            }
            if (cmpFunc(method, EntityRenderer$renderLabelIfPresent)) {
                // TODO should probably track these calls
                ret(ctx, null); // Function is void
                return;
            }

            // Other special-cased functions
            if (cmpFunc(method, MinecraftClient$getInstance)) {
                ret(ctx, root.cachedMinecraftClient);
                return;
            }
            if (cmpFunc(method, MinecraftClient$hasOutline)) {
                ret(ctx, new KnownInteger(false)); // Let's… not deal with that now
                return;
            }
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
            if (cmpFuncOrOverrides(method, Nameable$getName)) {
                ret(ctx, new UnknownValue("getName"));
                return;
            }
            if (cmpFuncOrOverrides(method, Nameable$getDisplayName)) {
                ret(ctx, new UnknownValue("getDisplayName"));
                return;
            }
            if (cmpFuncOrOverrides(method, Nameable$getCustomName)) {
                ret(ctx, new UnknownValue("getCustomName"));
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
            if (cmpFunc(method, DataTracker$getEntry)) {
                ret(ctx, AsmUtils.mockVmObjectRemap(ctx.machine(), "net.minecraft.class_2945$class_2946"));
                return;
            }
            if (cmpFunc(method, LivingEntityRenderer$shouldFlipUpsideDown)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> new KnownInteger(1))); // TODO
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesI)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(int.class)))));
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesF)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(float.class)))));
                return;
            }
            if (cmpFunc(method, MathHelper$wrapDegreesD)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> StackEntry.known(MathHelper.wrapDegrees(stackEntry.extractAs(double.class)))));
                return;
            }
            if (cmpFunc(method, Entity$removeClickEvents)) {
                ret(ctx, new UnaryArbitraryOp(arguments[0], stackEntry -> stackEntry)); // TODO
                return;
            }
            if (cmpFunc(method, Entity$isInPose)) {
                ret(ctx, new BinaryArbitraryOp(arguments[0], arguments[1], (stackEntry, stackEntry2) -> new KnownInteger(false))); // TODO
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
            // We're going to clone the vm to create a parallel universe / continuation
            // The clone will take the jump, and we won't

            var continuationNoJump = new ExecutionGraphNode();
            var continuationJump = new ExecutionGraphNode();
            var ifStmnt = new ExecutionGraphNode.IfStatement(compA, compB, opcode, continuationNoJump, continuationJump);
            this.node.setContinuation(ifStmnt);

            var vmNoJump = ctx.machine();
            var vmJump = ctx.machine().copy();

            vmNoJump.changeConfig(new RendererAnalyzerVmConfig(continuationNoJump, root));
            vmJump.changeConfig(new RendererAnalyzerVmConfig(continuationJump, root));

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
