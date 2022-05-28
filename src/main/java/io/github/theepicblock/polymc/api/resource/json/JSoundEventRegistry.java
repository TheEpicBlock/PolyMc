package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.json.JSoundEventRegistryImpl;

import java.util.Map;

public interface JSoundEventRegistry extends PolyMcAsset {
    Map<String, JSoundEvent> getMap();

    @Override
    default void importRequirements(ModdedResources from, PolyMcResourcePack to, SimpleLogger logger) {
        this.getMap().forEach((eventName, event) -> to.importRequirements(from, event, logger));
    }

    static JSoundEventRegistry create() {
        return new JSoundEventRegistryImpl();
    }
}
