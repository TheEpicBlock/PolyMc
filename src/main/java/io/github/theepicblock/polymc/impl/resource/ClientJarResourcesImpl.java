package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ClientJarResources;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.InputSupplier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;

public class ClientJarResourcesImpl implements ClientJarResources {
    // Retrievable by using the getClientSha1 gradle task
    // Or you can look around https://launchermeta.mojang.com/mc/game/version_manifest_v2.json
    private final static String CLIENT_SHA1 = "977727ec9ab8b4631e5c12839f064092f17663f8";
    private final static String CLIENT_URL = "https://piston-data.mojang.com/v1/objects/" + CLIENT_SHA1 + "/client.jar";

    private final JarFile clientJar;

    public ClientJarResourcesImpl(SimpleLogger logger) throws IOException {
        var file = getJarPath().toFile();
        if (!file.exists()) {
            downloadJar(file, logger);
        }
        this.clientJar = new JarFile(file);
    }

    @Override
    public @Nullable InputSupplier<InputStream> getInputStreamSupplier(String namespace, String path) {
        if (containsAsset(namespace, path)) {
            return () -> getInputStream(namespace, path);
        } else {
            return null;
        }
    }

    @Override
    public InputStream getInputStream(String namespace, String path) {
        var entry = clientJar.getEntry(ResourceConstants.ASSETS+namespace+"/"+path);
        if (entry == null) return null;
        try {
            return clientJar.getInputStream(entry);
        } catch (IOException ignored) {}
        return null;
    }

    @Override
    public boolean containsAsset(String namespace, String path) {
        return clientJar.getJarEntry(ResourceConstants.ASSETS+namespace+"/"+path) != null;
    }

    @Override
    public Set<Pair<Identifier,InputSupplier<InputStream>>> locateLanguageFiles() {
        throw new NotImplementedException();
    }

    public static void downloadJar(File location, SimpleLogger logger) throws IOException {
        logger.info("PolyMc is automatically downloading the vanilla client jar to access a few of it's resources. This may take a while.");
        FileUtils.forceMkdirParent(location);
        FileUtils.copyURLToFile(new URL(CLIENT_URL), location, 10000, 10000);
        logger.info("Finished downloading the minecraft jar");
    }

    public static Path getJarPath() {
        var loader = FabricLoader.getInstance();
        if (loader.getEnvironmentType() == EnvType.CLIENT) {
            try {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                return Path.of(clientFile);
            } catch (URISyntaxException ignored) {}
        }

        // We are using the same location as polymer uses, this ensures that the jar isn't downloaded twice
        return loader.getGameDir().resolve("polymer/cached_client_jars/" + CLIENT_SHA1 + ".jar");
    }

    @Override
    public void close() throws Exception {
        clientJar.close();
    }
}
