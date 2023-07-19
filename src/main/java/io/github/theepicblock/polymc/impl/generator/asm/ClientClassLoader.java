package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.common.collect.Streams;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.resource.ClientJarResourcesImpl;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.stream.Stream;

public class ClientClassLoader extends URLClassLoader {
    private final TeensyRemapper remapper;
    public final Mapping runtimeToObf = Mapping.runtimeToObfFromClasspath();
    private final HashMap<String, ClassNode> cache = new HashMap<>();

    public ClientClassLoader() {
        super("PolyMc auto generated classloader", getUrls(), ClientClassLoader.getSystemClassLoader());

        PolyMc.LOGGER.info("Current runtime mappings: "+FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
        FabricLoader.getInstance().getMappingResolver().getNamespaces().forEach(PolyMc.LOGGER::info);

        // The client jar is in official mappings, and needs to be transformed into official
        this.remapper = new TeensyRemapper("official");
    }
    
    static URL[] getUrls() {
        var mods = FabricLoader.getInstance()
            .getAllMods()
            .stream()
            .flatMap(mod -> mod.getRootPaths().stream())
            .map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            });
        
        var mcJar = ClientJarResourcesImpl.getJarPath().toFile();
        if (!mcJar.exists()) {
            try {
                ClientJarResourcesImpl.downloadJar(mcJar, PolyMc.LOGGER);
            } catch (IOException e) {
                throw new AssertionError(e);
            } 
        }

        try {
            return Streams.concat(mods, Stream.of(mcJar.toURL())).toArray(URL[]::new);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The returned class is guaranteed to be in the current runtime mappings
     * as defined by {@link net.fabricmc.loader.api.MappingResolver#getCurrentRuntimeNamespace}
     */
    public ClassNode getClass(@InternalName @NotNull String clazz) throws IOException {
        var c = cache.get(clazz);
        if (c != null) return c;

        var resource = this.getResourceAsStream(clazz + ".class");
        if (resource != null) {
            var node = new ClassNode(Opcodes.ASM9);
            new ClassReader(resource).accept(node, 0);
            cache.put(clazz, node);
            return node;
        }

        // This might be a client-only class, in which case it'll be in the client jar
        // The client jar is attached, but it's still using obfuscated mapping
        // So we try to see if the obfuscated name returns anything
        var obf = runtimeToObf.getClassname(clazz.replace("/", "."));

        resource = this.getResourceAsStream(obf.replace(".", "/") + ".class");
        if (resource != null) {
            // As this is part of the client jar, it needs to be remapped to
            // runtime mappings first
            var node = remapper.remapPls(resource);
            cache.put(clazz, node);
            return node;
        }

        throw new IOException("Class "+clazz+" ("+obf+") not found");
    }
}
