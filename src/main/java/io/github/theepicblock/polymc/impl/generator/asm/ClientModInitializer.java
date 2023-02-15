package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.Context;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine.VmConfig;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.fabricmc.loader.api.FabricLoader;

public class ClientModInitializer {    
    public static void runAnalysis(ClientClassLoader classLoader) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        var loader = FabricLoader.getInstance();
        var lClass = loader.getClass();
        var storage = lClass.getDeclaredField("entrypointStorage");
        storage.setAccessible(true);
        var entrypointStorage = storage.get(loader);
        
        var sClass = entrypointStorage.getClass();
        var getMethod = sClass.getDeclaredMethod("getOrCreateEntries", String.class);
        getMethod.setAccessible(true);
        var entrypoints = (List<Object>)getMethod.invoke(entrypointStorage, "client");

        var vm = new VirtualMachine(classLoader, (VmConfig) new VmConfig() {
            @Override
            public StackEntry invokeStatic(Context ctx, MethodInsnNode inst, List<Pair<Type, StackEntry>> arguments) throws VmException {
                if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityRendererRegistry")) {
                    PolyMc.LOGGER.info("EntityRendererRegistry: "+arguments);
                }
                return VmConfig.super.invokeStatic(ctx, inst, arguments);
            }
        });

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
            });;
    }

    public static void runAnalysis(String className, VirtualMachine vm) {
        try {
            PolyMc.LOGGER.info("OWKFoQP "+className);
            vm.runMethod(className, "onInitializeClient", "()V");
        } catch (VmException e) {
            e.printStackTrace();
        }
    }
}
