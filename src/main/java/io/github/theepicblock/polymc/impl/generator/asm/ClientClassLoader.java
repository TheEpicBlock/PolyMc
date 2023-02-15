package io.github.theepicblock.polymc.impl.generator.asm;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.resource.ClientJarResourcesImpl;
import net.fabricmc.loader.api.FabricLoader;

public class ClientClassLoader extends URLClassLoader {

    public ClientClassLoader() {
        super("PolyMc auto generated classloader", getUrls(), ClientClassLoader.getSystemClassLoader());
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
            throw new AssertionError(); // TODO
            // ClientJarResourcesImpl.downloadJar(mcJar, logger); 
        }

        try {
            return Streams.concat(mods, Stream.of(mcJar.toURL())).toArray(URL[]::new);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
