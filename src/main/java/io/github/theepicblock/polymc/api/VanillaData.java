package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

/**
 * Api to access the data PolyMc has on the blocks included in the vanilla game
 */
@ApiStatus.Experimental
public class VanillaData {
    /**
     * Checks how accurate this data is. When false, PolyMc is using heuristics. When true, PolyMc should be accurate 100% of the time
     */
    public static boolean isPerfectlyAccurate() {
        return ConfigManager.getConfig().remapVanillaBlockIds;
    }

    /**
     * Try to guess if this state is a vanilla state. Accuracy is determined by if the block id map was loaded or not (see {@link #isPerfectlyAccurate()}).
     * Even when guessing, PolyMc should never give false negatives (blocks that are marked as modded but are actually vanilla), only false positives.
     */
    public static boolean guessVanilla(BlockState state) {
        return Util.isVanilla(state);
    }
}
