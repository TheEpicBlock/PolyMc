package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.api.register.PolyRegister;

public class Generator {
    /**
     * Automatically generates all polys that are missing in the specified builder
     * @param builder builder to add polys to
     */
    public static void generateMissing(PolyRegister builder) {
        ItemPolyGenerator.generateMissing(builder);
    }
}
