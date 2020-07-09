package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.register.PolyRegister;
import io.github.theepicblock.polymc.generator.Generator;
import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class PolyMc implements ModInitializer {
    private static PolyMap map;
    public static final Logger LOGGER = Logger.getLogger("PolyMc");

    @Override
    public void onInitialize() {
        PolyMcCommands.registerCommands();
    }

    /**
     * Builds the poly map, this should only be run when all blocks/items have been registered.
     * This will be called by PolyMc when the worlds are generated.
     * @deprecated this is an internal method you shouldn't call
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void generatePolyMap() {
        PolyRegister builder = new PolyRegister();
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
