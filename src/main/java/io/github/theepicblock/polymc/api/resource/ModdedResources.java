package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ModdedResources extends AutoCloseable, ResourceContainer {

    /**
     * Gets all the files registered to this namespace:path. This is useful if a file can be defined in multiple places and need to be merged.
     * For example, there can be multiple 'minecraft:lang/en_us.json' definitions. The vanilla client will merge all of these
     */
    @NotNull List<InputStream> getInputStreams(String namespace, String path);

    /**
     * @return all namespaces that are in this combined resource pack
     */
    @NotNull Set<String> getAllNamespaces();

    @NotNull Set<Identifier> locateLanguageFiles();

    ClientJarResources getClientJar(SimpleLogger logger);

    /**
     * @return A view of the resources in this class combined with the resources you'd get from {@link #getClientJar(SimpleLogger)}
     */
    ModdedResources includeClientJar(SimpleLogger logger);
}
