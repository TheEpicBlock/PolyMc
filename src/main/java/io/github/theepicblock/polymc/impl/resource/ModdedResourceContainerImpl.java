package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import net.minecraft.util.Identifier;
import nl.theepicblock.resourcelocatorapi.ResourceLocatorApi;
import nl.theepicblock.resourcelocatorapi.api.AssetContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ModdedResourceContainerImpl implements ModdedResources {
    private final AssetContainer inner = ResourceLocatorApi.createGlobalAssetContainer();

    @Override
    public InputStream getInputStream(String namespace, String path) {
        try {
            return inner.getAsset(namespace, path);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<InputStream> getInputStreams(String namespace, String path) {
        try {
            return inner.getAllAssets(namespace, path);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getAllNamespaces() {
        return inner.getNamespaces();
    }

    @Override
    public Set<Identifier> locateLanguageFiles() {
        return inner.locateLanguageFiles();
    }

    @Override
    public boolean containsAsset(String namespace, String path) {
        return inner.containsAsset(namespace, path);
    }

    @Override
    public void close() throws Exception {
        inner.close();
    }
}
