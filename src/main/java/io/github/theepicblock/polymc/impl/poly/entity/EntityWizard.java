package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public abstract class EntityWizard<T extends Entity> extends Wizard {
    private final T entity;

    public EntityWizard(ServerWorld world, Vec3d position, T entity) {
        super(world, position, WizardState.MISC_MOVING);
        this.entity = entity;
    }

    public T getEntity() {
        return entity;
    }
}
