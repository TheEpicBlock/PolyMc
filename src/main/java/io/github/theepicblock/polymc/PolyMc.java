package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMapBuilder;
import io.github.theepicblock.polymc.generator.ItemGenerator;
import net.fabricmc.api.ModInitializer;

public class PolyMc {
    private static PolyMap map;

    public void generatePolyMap() {
        PolyMapBuilder builder = new PolyMapBuilder();
        //do entrypoint stuff :P
        ItemGenerator.generateAllItems(builder);

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
