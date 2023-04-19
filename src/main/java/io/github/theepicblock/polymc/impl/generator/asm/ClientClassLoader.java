package io.github.theepicblock.polymc.impl.generator.asm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.collect.Streams;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.resource.ClientJarResourcesImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

public class ClientClassLoader extends URLClassLoader {
    public ClientClassLoader() {
        super("PolyMc auto generated classloader", getUrls(), ClientClassLoader.getSystemClassLoader());

        PolyMc.LOGGER.info("Current runtime mappings: "+FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
        FabricLoader.getInstance().getMappingResolver().getNamespaces().forEach(efoin -> {
            PolyMc.LOGGER.info(efoin);
        });
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

    public InputStream getClass(String clazz) throws IOException {
        clazz = clazz.replace("/", ".");
        // Remap from intermediary into runtime mappings (also intermediary).
        // This will break with hashed intermediary
        clazz = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", clazz);

        var resource = this.getResourceAsStream(clazz.replace(".", "/") + ".class");
        if (resource != null) return resource;


        // This might be a client-only class, in which case it'll be in client jar
        // The client jar is attached, but it's still using obfuscated mapping
        // So we try to see if the obfuscated name returns anything
        var obf = FabricLoader.getInstance().getMappingResolver().unmapClassName("official", clazz);

        resource = this.getResourceAsStream(obf.replace(".", "/") + ".class");
        if (resource != null) return resource;
        
        throw new IOException("Class "+clazz+" ("+obf+") not found");
    }
}
