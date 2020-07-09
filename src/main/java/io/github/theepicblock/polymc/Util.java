package io.github.theepicblock.polymc;

import net.minecraft.util.Identifier;

public class Util {
    /**
     * Returns true if this identifier is in the minecraft namespace
     */
    public static boolean isVanilla(Identifier id) {
        return id.getNamespace().equals("minecraft");
    }
}
