package io.github.theepicblock.polymc.stubs.loader;

import io.github.theepicblock.polymc.stubs.ScreenStub;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.security.CodeSource;
import java.util.HashMap;

public class PolyMcStubsLoader implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger("PolyMcStubsLoader");
    private static final Class<?>[] STUBS = {
            ScreenStub.class
    };

    /**
     * The following code loads the stub class in {@link #STUBS} using asm.
     * The classes should be located in io.github.theepicblock.polymc.stubs.*
     * The classes are then read and used to generate a new class, with the name of
     * the class being replaced with the name of the Minecraft class we're stubbing.
     * This new class is then loaded by reflecting into KnotClassLoader
     */
    @Override
    public void onPreLaunch() {
        try {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) return;

            var mappings = FabricLoader.getInstance().getMappingResolver();
            var knot = PolyMcStubsLoader.class.getClassLoader();
            var defineClassMethod = knot.getClass().getDeclaredMethod("defineClassFwd", String.class, byte[].class, int.class, int.class, CodeSource.class);
            defineClassMethod.setAccessible(true);

            for (var stubClass : STUBS) {
                try {
                    var classMappingInfo = stubClass.getAnnotation(StubMappingInfo.class);

                    // Get the name of the target from intermediary in the current runtime mapping
                    var newName = mappings.mapClassName("intermediary", classMappingInfo.intermediary());

                    // Get the names of all the methods from intermediary in the current runtime mapping
                    var methodMappings = new HashMap<MethodRef, String>();
                    for (var method : stubClass.getMethods()) {
                        var methodMappingInfo = method.getAnnotation(StubMappingInfo.class);
                        if (methodMappingInfo == null) continue;
                        var original = new MethodRef(method.getName(), Type.getMethodDescriptor(method));
                        var newMethodName = mappings.mapMethodName(
                                "intermediary",
                                classMappingInfo.intermediary(),
                                methodMappingInfo.intermediary(),
                                original.descriptor());
                        methodMappings.put(original, newMethodName);
                    }

                    // Read the original class
                    var stubAsStream = stubClass.getResourceAsStream("/"+stubClass.getName().replace('.', '/') + ".class");
                    if (stubAsStream == null) {
                        LOGGER.error("Failed to read {}", stubClass.getName());
                        continue;
                    }

                    // Parse and remap the original class using asm
                    var reader = new ClassReader(stubAsStream);
                    var writer = new ClassWriter(0);
                    var adapter = new AsmRemapper(
                            writer,
                            newName.replace('.', '/'),
                            (name, descriptor) -> methodMappings.getOrDefault(new MethodRef(name, descriptor), name)
                    );
                    reader.accept(adapter, 0); // Send the class through the adapter which will pass it through to the writer where the code will be stored

                    // Load the new class
                    var newClass = writer.toByteArray();
                    defineClassMethod.invoke(knot, newName, newClass, 0, newClass.length, null);

                    LOGGER.info("Loaded PolyMc stub {} as {}", stubClass.getName(), newName);
                } catch (IOException e) {
                    LOGGER.error("IO Exception whilst reading {} using asm", stubClass.getName());
                    e.printStackTrace();
                } catch (ClassFormatError e) {
                    LOGGER.error("Something went wrong during asm processing of {}", stubClass.getName());
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Error loading stubs");
            e.printStackTrace();
        }
    }
}
