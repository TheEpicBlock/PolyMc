package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.Lambda;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StaticFieldValue;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class ClientInitializerAnalyzer {
    public static final SharedValuesKey<ClientInitializerAnalyzer> KEY = new SharedValuesKey<ClientInitializerAnalyzer>(ClientInitializerAnalyzer::new, null);

    private Map<Identifier, Lambda> entityRendererRegistry = new HashMap<>();
    private Map<Identifier, Lambda> entityModelLayerRegistry = new HashMap<>();

    public ClientInitializerAnalyzer(PolyRegistry registry) {
        this();
    }

    public ClientInitializerAnalyzer() {
        try {
            this.runAnalysis(new ClientClassLoader());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void runAnalysis(ClientClassLoader classLoader) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // Get entrypoints
        var loader = FabricLoader.getInstance();
        var lClass = loader.getClass();
        var storage = lClass.getDeclaredField("entrypointStorage");
        storage.setAccessible(true);
        var entrypointStorage = storage.get(loader);
        
        var sClass = entrypointStorage.getClass();
        var getMethod = sClass.getDeclaredMethod("getOrCreateEntries", String.class);
        getMethod.setAccessible(true);
        var entrypoints = (List<Object>)getMethod.invoke(entrypointStorage, "client");

        // Create vm

        Map<StackEntry, StackEntry> tmpEntityRendererRegistry = new HashMap<>();
        Map<StackEntry, StackEntry> tmpEntityModelLayerRegistry = new HashMap<>();

        var vm = new VirtualMachine(classLoader, (VmConfig) new VmConfig() {
            @Override
            public StackEntry loadStaticField(Context ctx, FieldInsnNode inst) throws VmException {
                return new StaticFieldValue(inst.owner, inst.name); // Evaluate static fields lazily
            }
            @Override
            public StackEntry invokeStatic(Context ctx, MethodInsnNode inst, Pair<Type, StackEntry>[] arguments) throws VmException {
                if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityRendererRegistry")) {
                    tmpEntityRendererRegistry.put(arguments[0].getRight(), arguments[1].getRight());
                    return new UnknownValue();
                }
                if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityModelLayerRegistry")) {
                    tmpEntityModelLayerRegistry.put(arguments[0].getRight(), arguments[1].getRight());
                    return new UnknownValue();
                }
                if (inst.owner.equals("net/fabricmc/fabric/impl/screenhandler/client/ClientNetworking") ||
                    inst.owner.equals("net/fabricmc/fabric/impl/event/interaction/InteractionEventsRouterClient")) {
                    // We do not care
                    return new UnknownValue();
                }
                // return new UnknownValue(); // Don't bother executing further
                try {
                    return VmConfig.super.invokeStatic(ctx, inst, arguments);
                } catch (VmException e) {
                    return new UnknownValue();
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
        
        // Resolve the tmp values
        tmpEntityModelLayerRegistry.entrySet().forEach(entry -> {
            resolveTmpMap(entry, vm, entityModelLayerRegistry);
        });
        tmpEntityRendererRegistry.entrySet().forEach(entry -> {
            resolveTmpMap(entry, vm, entityRendererRegistry);
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

    public void resolveTmpMap(Map.Entry<StackEntry, StackEntry> entry, VirtualMachine vm, Map<Identifier, Lambda> output) {
        var key = entry.getKey();
        if (key instanceof StaticFieldValue staticField) {
            // Resolve the static field
            try {
                var clazz = vm.getClass(staticField.owner());
                vm.ensureClinit(clazz);
                key = clazz.getStatic(staticField.field());
            } catch (VmException e) {
                PolyMc.LOGGER.error("Couldn't resolve static field");
                e.printStackTrace();
                return;
            }
        }
        Identifier identifier = null;
        if (key instanceof KnownObject o && o.i() instanceof Identifier i) {
            identifier = i;
        } else {
            PolyMc.LOGGER.error("Static field is weird "+key);
        }

        if (!(entry.getValue() instanceof Lambda l)) {
            PolyMc.LOGGER.error("Invalid param associated with "+identifier+": "+entry.getValue());
            return;
        }
        output.put(identifier, l);
    }
}
