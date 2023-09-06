package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class SharedValuesKey<T> {
    private final SharedValueFactory<T> factory;
    private final @Nullable ResourceContainerFactory<T> resourceContainerFactory;

    @ApiStatus.Experimental
    public SharedValuesKey(SharedValueFactory<T> factory, @Nullable ResourceContainerFactory<T> resourceContainerFactory) {
        this.factory = factory;
        this.resourceContainerFactory = resourceContainerFactory;
    }

    @ApiStatus.Internal
    public T createNew(PolyRegistry registry) {
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
        /**
         * @param registry Please don't keep this reference when moving to the resource container
         */
        T create(PolyRegistry registry);
    }

    @FunctionalInterface
    public interface ResourceContainerFactory<T> {
        /**
         * Please get rid of any references to any {@link PolyRegistry} at this point
         */
        ResourceContainer create(T sharedValues);
    }

    public interface ResourceContainer {
        void addToResourcePack(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger);

        default List<DebugDumpSection> addDebugSections() {
            return List.of();
        }
    }

    /**
     * Represents a section of a polydump
     */
    public record DebugDumpSection(String name, Consumer<StringBuilder> writer) {}
}
