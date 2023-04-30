package io.github.theepicblock.polymc.impl.generator.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.impl.generator.asm.ClientInitializerAnalyzer.EntityModelLayer;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.StaticFieldValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;

public class EntityRendererAnalyzer {
    public static final SharedValuesKey<EntityRendererAnalyzer> KEY = new SharedValuesKey<EntityRendererAnalyzer>(EntityRendererAnalyzer::new, null);

    private final ClientInitializerAnalyzer initializerInfo;
    private final VirtualMachine vm = new VirtualMachine(new ClientClassLoader(), (VmConfig) new VmConfig() {
        @Override
        public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
            return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
        }

        @Override
        public @Nullable StackEntry invoke(Context ctx, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
            // net.minecraft.client.render.entity.model.EntityModelLoader#getModelPart
            if (cmpFunc(inst, "net.minecraft.class_5599", "method_32072", "(Lnet/minecraft/class_5601;)Lnet/minecraft/class_630;")) {
                var modelLayer = arguments[1].resolve(ctx.machine()).cast(EntityModelLayer.class);
                var texturedModelDataProvider = initializerInfo.getEntityModelLayer(modelLayer);
                return vm.runMethod(texturedModelDataProvider, new StackEntry[0]);
            }

            try {
                return VmConfig.super.invoke(ctx, inst, arguments);
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
        var rendererFactory = initializerInfo.getEntityRenderer(entity);

        if (rendererFactory == null) {
            return;
        }

        // EntityRendererFactory.Context
        var ctx = AsmUtils.constructVmObject(vm, "net.minecraft.class_5617$class_5618")
            .f("renderDispatcher", null)
            .f("itemRenderer", null)
            .f("blockRenderManager", null)
            .f("heldItemRenderer", null)
            .f("resourceManager", null)
            .f("modelLoader", null)
            .f("textRenderer", null)
            .build();

        var renderer = vm.runMethod(rendererFactory, new StackEntry[]{ ctx });

        System.out.println("Renderer for "+entity.getTranslationKey()+": "+renderer);
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
}
