package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class SharedValuesKey<T> {
    private final SharedValueFactory<T> factory;
    private final @Nullable ResourceContainerFactory<T> resourceContainerFactory;

    public SharedValuesKey(SharedValueFactory<T> factory, @Nullable ResourceContainerFactory<T> resourceContainerFactory) {
        this.factory = factory;
        this.resourceContainerFactory = resourceContainerFactory;
    }

    @ApiStatus.Internal
    public T createNew(PolyRegistry registry) {
        if (this.hasResources()) registry = null; // Prevents there being a reference to PolyRegistry after it has been built
        return factory.create(registry);
    }

    public boolean hasResources() {
        return this.resourceContainerFactory != null;
    }

    public ResourceContainer createResources(T sharedValues) {
        if (this.resourceContainerFactory == null) return null;
        return this.resourceContainerFactory.create(sharedValues);
    }

    @FunctionalInterface
    public interface SharedValueFactory<T> {
        T create(PolyRegistry registry);
    }

    @FunctionalInterface
    public interface ResourceContainerFactory<T> {
        ResourceContainer create(T sharedValues);
    }

    @FunctionalInterface
    public interface ResourceContainer {
        void addToResourcePack(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger);
    }
}
