package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor.VmException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;

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
                runAnalysis(entrypoint, classLoader);
            });;
    }

    public static void runAnalysis(String className, ClientClassLoader loader) {
        try {
            PolyMc.LOGGER.info("OWKFoQP "+className);
            var stream = loader.getResourceAsStream(className.replace(".", "/") + ".class");

            // Load class using ASM
            var clientInitializerClass = new ClassNode(Opcodes.ASM9);
            new ClassReader(stream).accept(clientInitializerClass, 0);

            var init = AsmUtils.getMethod(clientInitializerClass, "onInitializeClient", "()V");
            if (init == null) {
                PolyMc.LOGGER.warn("Couldn't fine onInitializeClient in "+className);
            }
            
            // Load class in class loader
            // this should load all of the static fields
            var clazz = loader.loadClass(className);

            // Run class using virtual machine
            var vm = new MethodExecutor() {
                @Override
                public StackEntry loadStaticField(FieldInsnNode inst) {
                    try {
                        // Allow loading static fields
                        var clazz = loader.loadClass(AsmUtils.toBinary(inst.owner));
                        var field = clazz.getDeclaredField(inst.name);
                        field.setAccessible(true);
                        var value = field.get(null);
                        return MethodExecutor.knownStackValue(value);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException e) {}
                    return super.loadStaticField(inst);
                }

                @Override
                public StackEntry invokeStatic(MethodInsnNode inst, List<Pair<Type, StackEntry>> arguments) {
                    if (inst.owner.equals("net/fabricmc/fabric/api/client/rendering/v1/EntityRendererRegistry")) {
                        PolyMc.LOGGER.info("EntityRendererRegistry: "+arguments);
                    }
                    return super.invokeStatic(inst, arguments);
                }
            };
            
            try {
                vm.run(init.instructions);
            } catch (VmException e) {
                new Exception("Couldn't run client initializer for "+className, e).printStackTrace();;
            }

            PolyMc.LOGGER.info(clazz.getCanonicalName());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
