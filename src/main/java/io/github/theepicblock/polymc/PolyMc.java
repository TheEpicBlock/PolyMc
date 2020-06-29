package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.register.PolyMapBuilder;
import io.github.theepicblock.polymc.generator.Generator;

public class PolyMc {
    private static PolyMap map;

    /**
     * Builds the poly map, this should only be run when all blocks/items have been registered.
     * This will be called by PolyMc when the worlds are generated.
     * @deprecated this is an internal method you shouldn't call
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void generatePolyMap() {
        PolyMapBuilder builder = new PolyMapBuilder();
        //TODO let other mods generate items here via an entry point

        //Auto generate the rest
        Generator.generateMissing(builder);

        map = builder.build();
    }

    /**
     * Gets the polymap needed to translate from server items to client items.
     * @throws NullPointerException if you try to access it before the server worlds get initialized
     * @return the PolyMap
     */
    public static PolyMap getMap() {
        if (map == null) {
            throw new NullPointerException("Tried to access the PolyMap before it was initialized");
        }
        return map;
    }
}
