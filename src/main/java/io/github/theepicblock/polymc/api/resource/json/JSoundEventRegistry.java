package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;

import java.util.Map;

public interface JSoundEventRegistry extends PolyMcAsset {
    Map<String, JSoundEvent> getMap();

    @Override
    default void importRequirements(ModdedResources from, PolyMcResourcePack to) {
        this.getMap().forEach((eventName, event) -> to.importRequirements(from, event));
    }
}
