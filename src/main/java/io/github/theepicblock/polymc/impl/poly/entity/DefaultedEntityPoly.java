package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;

public class DefaultedEntityPoly<T extends Entity> implements EntityPoly<T> {
    private final EntityType<?> displayType;

    public DefaultedEntityPoly(EntityType<?> display) {
        this.displayType = display;
    }

    @Override
    public Wizard createWizard(WizardInfo info, T entity) {
        return new DefaultedEntityWizard<>(info, entity, this.displayType);
    }

    @Override
    public String getDebugInfo(EntityType<?> obj) {
        return displayType.getTranslationKey();
    }

    public static class DefaultedEntityWizard<T extends Entity> extends EntityWizard<T> {
        private final AbstractVirtualEntity virtualEntity;

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
            var original = this.getEntity();
            virtualEntity.spawn(player, this.getPosition(), original.getPitch(), original.getYaw(), 0, original.getVelocity());

            player.sendPacket(EntityUtil.createDataTrackerUpdate(
                    this.virtualEntity.getId(),
                    List.of(
                            new DataTracker.Entry<>(EntityAccessor.getCustomName(), Optional.of(this.getEntity().getName())),
                            new DataTracker.Entry<>(EntityAccessor.getNameVisible(), true))
                    )
            );
            
            sendStandardPackets(player, getEntity());
        }

        public static void sendStandardPackets(PacketConsumer player, Entity original) {
            var changedEntries = original.getDataTracker().getChangedEntries();
            if (changedEntries != null && !changedEntries.isEmpty()) {
                player.sendPacket(new EntityTrackerUpdateS2CPacket(original.getId(), changedEntries));
            }

            if (original instanceof LivingEntity e) {
                var attributes = e.getAttributes().getAttributesToSend();
                if (!attributes.isEmpty()) {
                    player.sendPacket(new EntityAttributesS2CPacket(original.getId(), attributes));
                }

                var list = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

                for(var equipmentSlot : EquipmentSlot.values()) {
                    var itemStack = e.getEquippedStack(equipmentSlot);
                    if (!itemStack.isEmpty()) {
                        list.add(Pair.of(equipmentSlot, itemStack.copy()));
                    }
                }

                if (!list.isEmpty()) {
                    player.sendPacket(new EntityEquipmentUpdateS2CPacket(e.getId(), list));
                }

                for(var statusEffect : e.getStatusEffects()) {
                    player.sendPacket(new EntityStatusEffectS2CPacket(e.getId(), statusEffect));
                }
            }

            if (!original.getPassengerList().isEmpty()) {
                player.sendPacket(new EntityPassengersSetS2CPacket(original));
            }
    
            if (original.hasVehicle()) {
                player.sendPacket(new EntityPassengersSetS2CPacket(original.getVehicle()));
            }
    
            if (original instanceof MobEntity mobEntity && mobEntity.isLeashed()) {
                player.sendPacket(new EntityAttachS2CPacket(mobEntity, mobEntity.getHoldingEntity()));
            }
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            virtualEntity.remove(player);
        }
    }
}
