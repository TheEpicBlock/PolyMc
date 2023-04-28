package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.Lambda;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StaticFieldValue;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientInitializerAnalyzer {
    public static final SharedValuesKey<ClientInitializerAnalyzer> KEY = new SharedValuesKey<ClientInitializerAnalyzer>(ClientInitializerAnalyzer::new, null);

    private Map<EntityType<?>, Lambda> entityRendererRegistry = new HashMap<>();
    private Map<EntityModelLayer, Lambda> entityModelLayerRegistry = new HashMap<>();

    public ClientInitializerAnalyzer(PolyRegistry registry) {
        this();
    }

    public ClientInitializerAnalyzer() {
        try {
            this.runAnalysis(new ClientClassLoader());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void runAnalysis(ClientClassLoader classLoader) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        // Get entrypoints
        Object loader;
        if (FabricLoader.getInstance().isModLoaded("quilt_loader")) {
            loader = Class.forName("org.quiltmc.loader.impl.QuiltLoaderImpl").getField("INSTANCE").get(null);
        } else {
            loader = FabricLoader.getInstance();
        }
        var lClass = loader.getClass();
        var storage = lClass.getDeclaredField("entrypointStorage");
        storage.setAccessible(true);
        var entrypointStorage = storage.get(loader);
        
        var sClass = entrypointStorage.getClass();
        var getMethod = sClass.getDeclaredMethod("getOrCreateEntries", String.class);
        getMethod.setAccessible(true);
        var entrypoints = (List<Object>)getMethod.invoke(entrypointStorage, "client");

        // Create vm
        var vm = new VirtualMachine(classLoader, (VmConfig) new VmConfig() {
            @Override
            public @NotNull StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
                var fromEnvironment = AsmUtils.tryGetStaticFieldFromEnvironment(ctx, inst);
                if (fromEnvironment != null) return fromEnvironment; // Return the known value if the class is already loaded in the *actual* jvm
                return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
            }
            @Override
            public StackEntry invoke(Context ctx, MethodInsnNode inst, StackEntry[] arguments) throws VmException {
                try {
                    if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityRendererRegistry")) {
                        var identifier = arguments[0].resolve(ctx.machine()).cast(EntityType.class);
                        var lambda = arguments[1].resolve(ctx.machine()).cast(Lambda.class);
                        entityRendererRegistry.put(identifier, lambda);
                        return new UnknownValue();
                    }
                    if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityModelLayerRegistry")) {
                        var modelLayer = arguments[0].resolve(ctx.machine()).cast(EntityModelLayer.class);
                        var lambda = arguments[1].resolve(ctx.machine()).cast(Lambda.class);
                        entityModelLayerRegistry.put(modelLayer, lambda);
                        return new UnknownValue();
                    }
                } catch(Throwable e) {
                    PolyMc.LOGGER.error("Failed to resolve arguments for call to "+inst.owner+": "+Arrays.toString(arguments));
                    e.printStackTrace();
                    return new UnknownValue();
                }
                if (inst.owner.equals("net/fabricmc/fabric/impl/screenhandler/client/ClientNetworking") ||
                    inst.owner.equals("net/fabricmc/fabric/impl/event/interaction/InteractionEventsRouterClient")) {
                    // We do not care
                    return new UnknownValue();
                }
                try {
                    return VmConfig.super.invoke(ctx, inst, arguments);
                } catch (VmException e) {
                    PolyMc.LOGGER.error("Couldn't run "+inst.owner+"#"+inst.name+": "+e.createFancyErrorMessage());
                    return new UnknownValue(e);
                }
            }
        });

        // Execute entrypoints in vm
        entrypoints.stream().map(entrypoint -> {
                try {
                    var eClass = entrypoint.getClass();
                    var valueField = eClass.getDeclaredField("value");
                    valueField.setAccessible(true);
                    return (String)valueField.get(entrypoint);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(e -> e != null)
            .forEach(entrypoint -> {
                runAnalysis(entrypoint, vm);
            });

        entityRendererRegistry.forEach((a,b) -> {
            PolyMc.LOGGER.info("entity renderer "+Registries.ENTITY_TYPE.getId(a)+" -> "+b.method());
        });
        entityModelLayerRegistry.forEach((a,b) -> {
            PolyMc.LOGGER.info("entity model layer "+a+" -> "+b.method());
        });
    }

    public void runAnalysis(String className, VirtualMachine vm) {
        try {
            PolyMc.LOGGER.info("OWKFoQP "+className);
            var methodName = "onInitializeClient";
            if (className.contains("::")) {
                var s = className.split("::");
                className = s[0];
                methodName = s[1];
            }
            vm.runMethod(className, methodName, "()V");
        } catch (VmException e) {
            e.printStackTrace();
        }
    }

    public Lambda getEntityRenderer(EntityType<?> type) {
        return entityRendererRegistry.get(type);
    }

    public Lambda getEntityModelLayer(EntityModelLayer layer) {
        return entityModelLayerRegistry.get(layer);
    }

    public static record EntityModelLayer(Identifier id, String name) {

    }
}
