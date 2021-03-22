package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public abstract class Wizard implements WatchListener {
    private Vec3d position;
    private WizardState state;

    public Wizard(Vec3d position) {
        this.position = position;
    }

    public void onRemove() {
        this.removeAllPlayers();
    }

    public void onMove() {}
    public void onStateChange() {}

    public final Vec3d getPosition() {
        return position;
    }

    public final void setPosition(Vec3d position) {
        this.position = position;
        this.onMove();
    }

    public final WizardState getState() {
        return state;
    }

    public final void setState(WizardState state) {
        this.state = state;
        this.onStateChange();
    }

    public enum WizardState {
        BLOCK
    }
}
