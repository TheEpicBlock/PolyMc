package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import nl.theepicblock.resourcelocatorapi.ResourceLocatorApi;
import nl.theepicblock.resourcelocatorapi.api.ExtendedResourcePack;

import java.io.IOException;
import java.io.InputStream;

public class ModdedResourceContainerImpl implements ModdedResources {
    private final ExtendedResourcePack inner = ResourceLocatorApi.createGlobalResourcePack();

    @Override
    public InputStream getInputStream(String namespace, String path) {
        try {
            return inner.getAsset(namespace, path);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean containsAsset(String namespace, String path) {
        return inner.containsAsset(namespace, path);
    }
}
