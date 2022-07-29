package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.VSnowball;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;

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
        }

        @Override
        public void onMove(PacketConsumer players) {
            var entity = this.getEntity();
            snowball.move(players, this.getPosition(), entity.getYaw(), entity.getPitch(), entity.isOnGround());
            snowball.sendVelocity(players, entity.getVelocity());
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            snowball.remove(player);
        }
    }
}
