package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.*;
import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;

import java.util.List;
import java.util.Optional;

public class DefaultedEntityPoly<T extends Entity> implements EntityPoly<T> {
    private final EntityType<?> displayType;

    public DefaultedEntityPoly(EntityType<?> display) {
        this.displayType = display;
    }

    @Override
    public Wizard createWizard(WizardInfo info, T entity) {
        return new DefaultedEntityWizard<>(info, entity, this.displayType);
    }

    public static class DefaultedEntityWizard<T extends Entity> extends EntityWizard<T> {
        private final VirtualEntity virtualEntity;

        public DefaultedEntityWizard(WizardInfo info, T entity, EntityType<?> type) {
            super(info, entity);
            virtualEntity = new AbstractVirtualEntity(entity.getUuid(), entity.getId()) {
                @Override
                public EntityType<?> getEntityType() {
                    return type;
                }
            };
        }

        @Override
        public void onMove(PacketConsumer players) {
        }

        @Override
        public void addPlayer(PacketConsumer player) {
            virtualEntity.spawn(player, this.getPosition());

            player.sendPacket(EntityUtil.createDataTrackerUpdate(
                    this.virtualEntity.getId(),
                    List.of(
                            new DataTracker.Entry<>(EntityAccessor.getCustomName(), Optional.of(this.getEntity().getName())),
                            new DataTracker.Entry<>(EntityAccessor.getNameVisible(), true))
                    )
            );
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            virtualEntity.remove(player);
        }
    }
}
