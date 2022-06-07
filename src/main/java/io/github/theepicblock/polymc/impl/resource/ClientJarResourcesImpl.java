package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ClientJarResources;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
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
    private final static String CLIENT_SHA1 = "c0898ec7c6a5a2eaa317770203a1554260699994";
    private final static String CLIENT_URL = "https://launcher.mojang.com/v1/objects/" + CLIENT_SHA1 + "/client.jar";

    private final JarFile clientJar;

    public ClientJarResourcesImpl(SimpleLogger logger) throws IOException {
        var file = getJarPath().toFile();
        if (!file.exists()) {
            downloadJar(file, logger);
        }
        this.clientJar = new JarFile(file);
    }

    @Override
    public @Nullable InputStream getInputStream(String namespace, String path) {
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
    public Set<Identifier> locateLanguageFiles() {
        throw new NotImplementedException();
    }

    public static void downloadJar(File location, SimpleLogger logger) throws IOException {
        logger.info("Something is requesting access to the vanilla client jar's resources. " +
                "PolyMc is automatically downloading them.");
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

        // This location was copied from Polymer to prevent the client jar from being downloaded twice.
        return loader.getGameDir().resolve("polymer_cache/client_jars/" + CLIENT_SHA1 + ".jar");
    }

    @Override
    public void close() throws Exception {
        clientJar.close();
    }
}
