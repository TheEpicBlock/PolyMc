package io.github.theepicblock.polymc.api.register;

import io.github.theepicblock.polymc.api.PolyMap;

/**
 * Makes a {@link PolyMap}
 * It contains all the utilities needed to manage the different Poly types.
 */
public class PolyMapBuilder {
    private final ItemPolyMapBuilder itemBuilder = new ItemPolyMapBuilder();

    public ItemPolyMapBuilder getItem() {
        return itemBuilder;
    }

    public PolyMap build() {
        //TODO fix
        return new PolyMap(itemBuilder.build());
    }
}
