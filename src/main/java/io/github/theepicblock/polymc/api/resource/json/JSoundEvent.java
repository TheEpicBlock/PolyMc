package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;

import java.util.List;

/**
 * @see JSoundEventRegistry
 */
public interface JSoundEvent extends AssetWithDependencies {
    boolean getReplace();
    void setReplace(boolean v);

    String getSubtitle();
    void setSubtitle(String v);

    /**
     * Modifying this list won't do anything. You must use {@link #setSounds()} when done.
     */
    List<JSoundReference> getSounds();
    void setSounds(List<JSoundReference> newSounds);

    @Override
    default void importRequirements(ModdedResources from, PolyMcResourcePack to) {
        this.getSounds().forEach((soundRef) -> to.importRequirements(from, soundRef));
    }
}
