package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * See {@link io.github.theepicblock.polymc.PolyMc} for where these are ticked from
 * See {@link WorldChunkMixin} for where the wizards are put into the list
 */
@Mixin(ServerWorld.class)
public class WorldMixin implements WizardTickerDuck {
    @Unique
    private final List<Wizard> tickingWizards = new ArrayList<>();

    @Override
    public void polymc$addTicker(Wizard wizard) {
        if (wizard.needsTicking()) {
            tickingWizards.add(wizard);
        }
    }

    @Override
    public void polymc$removeTicker(Wizard wizard) {
        if (wizard.needsTicking()) {
            tickingWizards.remove(wizard);
        }
    }

    @Override
    public List<Wizard> polymc$getTickers() {
        return tickingWizards;
    }
}
