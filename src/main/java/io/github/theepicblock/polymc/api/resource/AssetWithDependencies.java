package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;

public interface AssetWithDependencies {
    default void importRequirements(ModdedResources from, PolyMcResourcePack to, SimpleLogger logger) {

    }
}
