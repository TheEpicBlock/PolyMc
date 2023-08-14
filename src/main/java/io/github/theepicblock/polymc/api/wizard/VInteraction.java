package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.InteractionEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;

import java.util.List;
import java.util.UUID;

public class VInteraction extends AbstractVirtualEntity {
    public VInteraction() {
        super();
    }

    public VInteraction(UUID uuid, int id) {
        super(uuid, id);
    }

    @Override
    public EntityType<?> getEntityType() {
        return EntityType.INTERACTION;
    }

    public void sendWidth(PacketConsumer player, float width) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.getId(),
                InteractionEntityAccessor.getWIDTH(),
                width
        ));
    }

    public void sendHeight(PacketConsumer player, float height) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.getId(),
                InteractionEntityAccessor.getHEIGHT(),
                height
        ));

    }

    /**
     * If response is set to true, left-clicking an interaction entity plays a punching sound,
     * and right-clicking it makes the player's arm swing
     * @see <a href="https://minecraft.fandom.com/wiki/Interaction#Behavior">wiki</a>
     */
    public void sendResponse(PacketConsumer player, boolean responds) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.getId(),
                InteractionEntityAccessor.getRESPONSE(),
                responds
        ));
    }

    public void setup(PacketConsumer player, float width, float height, boolean responds) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.getId(),
                List.of(
                        new DataTracker.Entry<>(InteractionEntityAccessor.getWIDTH(), width),
                        new DataTracker.Entry<>(InteractionEntityAccessor.getHEIGHT(), height),
                        new DataTracker.Entry<>(InteractionEntityAccessor.getRESPONSE(), responds)
                )
        ));
    }
}
