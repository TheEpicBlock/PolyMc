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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class EntityRendererAnalyzer {
    public static final SharedValuesKey<EntityRendererAnalyzer> KEY = new SharedValuesKey<EntityRendererAnalyzer>(EntityRendererAnalyzer::new, null);

    private final ClientInitializerAnalyzer initializerInfo;

    /**
     * Vm for invoking the factory
     */
    private final VirtualMachine factoryVm = new VirtualMachine(new ClientClassLoader(), (VmConfig) new VmConfig() {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
        }

        @Override
        public @Nullable StackEntry invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            // net.minecraft.client.render.entity.model.EntityModelLoader#getModelPart
            if (cmpFunc(inst, "net.minecraft.class_5599", "method_32072", "(Lnet/minecraft/class_5601;)Lnet/minecraft/class_630;")) {
                var fmappings = FabricLoader.getInstance().getMappingResolver();
                var modelLayer = arguments[1].resolve(ctx.machine()).cast(EntityModelLayer.class);
                var texturedModelDataProvider = initializerInfo.getEntityModelLayer(modelLayer);
                var texturedModelData = factoryVm.runMethod(texturedModelDataProvider, new StackEntry[0]);
                var texturedModelData$createModel = AsmUtils.mapAll(fmappings, "net.minecraft.class_5607", "method_32109", "()Lnet/minecraft/class_3879;");
                return factoryVm.runMethod(factoryVm.resolveMethod(null, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, texturedModelData$createModel.clazz(), texturedModelData$createModel.method(), texturedModelData$createModel.desc(), false), texturedModelData), new StackEntry[]{ texturedModelData });
            }

            try {
                return VmConfig.super.invoke(ctx, currentClass, inst, arguments);
            } catch (VmException e) {
                PolyMc.LOGGER.error("Couldn't run "+inst.owner+"#"+inst.name+": "+e.createFancyErrorMessage());
                return new UnknownValue(e);
            }
        }
    });

    private final VirtualMachine rendererVm = new VirtualMachine(new ClientClassLoader(), (VmConfig) new VmConfig() {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
        }

        @Override
        public @Nullable StackEntry invoke(Context ctx, Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            if (cmpFunc(inst, "net.minecraft.class_630.class_628", "method_32089", "(Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_4588;IIFFFF)V")) {
                var cuboid = arguments[0].cast(Cuboid.class);
                System.out.println("Rendering a cute lil cuboid: "+cuboid);
                return new UnknownValue();
            }

            try {
                return VmConfig.super.invoke(ctx, currentClass, inst, arguments);
            } catch (VmException e) {
                PolyMc.LOGGER.error("Couldn't run "+inst.owner+"#"+inst.name+": "+e.createFancyErrorMessage());
                return new UnknownValue(e);
            }
        }
    });

    public EntityRendererAnalyzer(PolyRegistry registry) {
        this.initializerInfo = registry.getSharedValues(ClientInitializerAnalyzer.KEY);
    }

    public void analyze(EntityType<?> entity) throws VmException {
        var fmappings = FabricLoader.getInstance().getMappingResolver();
        var rendererFactory = initializerInfo.getEntityRenderer(entity);

        if (rendererFactory == null) {
            return;
        }

        // EntityRendererFactory.Context
        var ctx = AsmUtils.constructVmObject(factoryVm, "net.minecraft.class_5617$class_5618")
            .mockField("renderDispatcher")
            .mockField("itemRenderer")
            .mockField("blockRenderManager")
            .mockField("heldItemRenderer")
            .mockField("resourceManager")
            .mockField("modelLoader")
            .mockField("textRenderer")
            .build();

        var renderer = factoryVm.runMethod(rendererFactory, new StackEntry[]{ ctx });

        System.out.println("Renderer for "+entity.getTranslationKey()+": "+renderer);

        // We need to create a fake instruction that's calling the render method so we can resolve it
        var entityRenderer$render = AsmUtils.mapAll(fmappings, "net.minecraft.class_897", "method_3936", "(Lnet/minecraft/class_1297;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
        var inst = new MethodInsnNode(Opcodes.INVOKEINTERFACE, entityRenderer$render.clazz(), entityRenderer$render.method(), entityRenderer$render.desc(), false);
        var resolvedEntityRenderer$render = factoryVm.resolveMethod(null, inst, renderer);

        var entityClass = InternalEntityHelpers.getEntityClass(entity);
        entityClass = entityClass == null ? Entity.class : entityClass;
        var fakeEntity = AsmUtils.constructVmObject(factoryVm, entityClass)
            .build();
        var matrixStack = createMatrixStack();

        // TODO all of these variables should be dynamic/symbolic
        rendererVm.runMethod(resolvedEntityRenderer$render, new StackEntry[] { renderer, fakeEntity, new KnownFloat(0.0f), new KnownFloat(0.0f), matrixStack, KnownObject.NULL, new KnownInteger(0) });
    }

    public StackEntry createMatrixStack() throws VmException {
        var fmapper = FabricLoader.getInstance().getMappingResolver();
        var matrixStackInt = "net.minecraft.class_4587";
        var matrixStackRun = fmapper.mapClassName("intermediary", matrixStackInt).replace(".", "/");
        var matrixStack = AsmUtils.constructVmObject(factoryVm, "net.minecraft.class_4587").build();
        factoryVm.runMethod(factoryVm.getClass(matrixStackRun), "<init>", "()V", new StackEntry[] { matrixStack });

        return matrixStack;
    }

    /**
     * converts intermediary to obfuscated
     */
    public boolean cmpFunc(MethodInsnNode inst, String intermediaryClassname, String methodName, String methodDesc) {
        var fmapper = FabricLoader.getInstance().getMappingResolver();
        
        var instClass = fmapper.mapClassName("official", inst.owner.replace("/", "."));
        var clazz = fmapper.mapClassName("intermediary", intermediaryClassname);

        if (!instClass.equals(clazz)) return false;

        var instMethodName = fmapper.mapMethodName("official", inst.owner.replace("/", "."), inst.name, inst.desc);
        var meadaefafqwuuvgy = fmapper.mapMethodName("intermediary", intermediaryClassname, methodName, methodDesc); // I was tired whilst I wrote this

        return instMethodName.equals(meadaefafqwuuvgy);
    }

    public static record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }
}
