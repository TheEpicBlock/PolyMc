package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ClientJarResources;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import nl.theepicblock.resourcelocatorapi.ResourceLocatorApi;
import nl.theepicblock.resourcelocatorapi.api.AssetContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ModdedResourceContainerImpl implements ModdedResources {
    private final AssetContainer inner = ResourceLocatorApi.createGlobalAssetContainer();
    private ClientJarResourcesImpl clientJar = null;

    @Override
    public @Nullable InputSupplier<InputStream> getInputStreamSupplier(String namespace, String path) {
        return inner.getAsset(namespace, path);
    }

    @Override
    public @NotNull List<InputSupplier<InputStream>> getInputStreams(String namespace, String path) {
        return inner.getAllAssets(namespace, path);
    }

    @Override
    public @NotNull Set<String> getAllNamespaces() {
        return inner.getNamespaces();
    }

    @Override
    public @NotNull Set<Pair<Identifier,InputSupplier<InputStream>>> locateLanguageFiles() {
        return inner.locateLanguageFiles();
    }

    @Override
    public ClientJarResources getClientJar(SimpleLogger logger) {
        if (clientJar == null) {
            try {
                clientJar = new ClientJarResourcesImpl(logger);
            } catch (IOException e) {
                logger.error("Failed to get the client jar");
                e.printStackTrace();
            }
        }
        return this.clientJar;
    }

    @Override
    public ModdedResources includeClientJar(SimpleLogger logger) {
        return new MergedModdedResources(this, this.getClientJar(logger));
    }

    @Override
    public boolean containsAsset(String namespace, String path) {
        return inner.containsAsset(namespace, path);
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    @Override
    public void close() throws Exception {
        inner.close();
        if (clientJar != null) {
            clientJar.close();
        }
    }
}
