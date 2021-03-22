package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldWizardViewProvider implements WizardView {
    @Shadow public abstract WorldChunk getChunk(int i, int j);

    @Override
    public PolyMapMap<Wizard> getWizards(BlockPos pos) {
        WorldChunk worldChunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return ((WizardView)worldChunk).getWizards(pos);
    }

    @Override
    public PolyMapMap<Wizard> removeWizards(BlockPos pos, boolean move) {
        WorldChunk worldChunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return ((WizardView)worldChunk).removeWizards(pos, move);
    }
}
