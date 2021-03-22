package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public abstract class Wizard implements WatchListener {
    private Vec3d position;
    private WizardState state;

    public Wizard(Vec3d position, WizardState state) {
        this.position = position;
        this.state = state;
    }

    public void onRemove() {
        this.removeAllPlayers();
    }

    public void onMove() {}
    public void onStateChange() {}

    public final Vec3d getPosition() {
        return position;
    }

    public final void updatePosition(Vec3d position) {
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
        /**
         * The wizard is a block in the world
         */
        BLOCK,
        /**
         * The wizard is attached to a falling block entity
         */
        FALLING_BLOCK,
        /**
         * The wizard is being moved by a piston
         */
        MOVING_PISTON,
        /**
         * The wizard is in some other state that can move
         */
        MOVING_OTHER;

        private boolean isStatic() {
            return this == BLOCK;
        }
    }
}
