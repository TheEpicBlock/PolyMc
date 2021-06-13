package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface WizardView {
    /**
     * @see #getWizards(BlockPos)
     */
    static PolyMapMap<Wizard> getWizards(World world, BlockPos pos) {
        return ((WizardView)world).getWizards(pos);
    }

    /**
     * @see #removeWizards(BlockPos, boolean)
     */
    static PolyMapMap<Wizard> removeWizards(World world, BlockPos pos, boolean move) {
        return ((WizardView)world).removeWizards(pos, move);
    }

    /**
     * Gets all wizards at a position
     * @param pos position to get the wizards from.
     * @return A polymapmap of polymap -> wizard of all wizards in this location
     */
    PolyMapMap<Wizard> getWizards(BlockPos pos);

    /**
     * Removes a wizard at a position
     * @param pos  position to remove the wizards from.
     * @param move when true. Don't notify the wizard to remove it from the world.
     * @return the wizards that were at this position. An empty map if there were none.
     */
    PolyMapMap<Wizard> removeWizards(BlockPos pos, boolean move);
}
