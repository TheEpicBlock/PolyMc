package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.VSnowball;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.data.DataTracker;

import java.util.List;
import java.util.Optional;

public class FlyingItemEntityPoly<T extends Entity & FlyingItemEntity> implements EntityPoly<T> {
    @Override
    public Wizard createWizard(WizardInfo info, T entity) {
        return new FlyingItemEntityWizard<T>(info, entity);
    }

    public static class FlyingItemEntityWizard<T extends Entity & FlyingItemEntity> extends EntityWizard<T> {
        private final VSnowball snowball;

        public FlyingItemEntityWizard(WizardInfo info, T entity) {
            super(info, entity);
            this.snowball = new VSnowball();
        }

        @Override
        public void addPlayer(PacketConsumer player) {
            snowball.spawn(player, this.getPosition());
            snowball.sendItem(player, this.getEntity().getStack());
            player.sendPacket(EntityUtil.createDataTrackerUpdate(
                    snowball.getId(),
                    List.of(
                            new DataTracker.Entry<>(EntityAccessor.getCustomName(), Optional.of(this.getEntity().getName())),
                            new DataTracker.Entry<>(EntityAccessor.getNameVisible(), true))
                    )
            );
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            snowball.remove(player);
        }
    }
}
