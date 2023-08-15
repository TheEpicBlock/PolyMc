package io.github.theepicblock.polymc.api.wizard;

import io.github.theepicblock.polymc.impl.poly.wizard.AbstractVirtualEntity;
import io.github.theepicblock.polymc.impl.poly.wizard.EntityUtil;
import io.github.theepicblock.polymc.mixins.wizards.DisplayEntityAccessor;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.math.AffineTransformation;

import java.util.List;
import java.util.UUID;

public abstract class AbstractVDisplay extends AbstractVirtualEntity {
    public AbstractVDisplay() {
        super();
    }

    public AbstractVDisplay(UUID uuid, int id) {
        super(uuid, id);
    }

    public void setupTransforms(PacketConsumer player, AffineTransformation transformation) {
        player.sendPacket(EntityUtil.createDataTrackerUpdate(
                this.getId(),
                List.of(
                        new DataTracker.Entry<>(DisplayEntityAccessor.getTRANSLATION(), transformation.getTranslation()),
                        new DataTracker.Entry<>(DisplayEntityAccessor.getSCALE(), transformation.getScale()),
                        new DataTracker.Entry<>(DisplayEntityAccessor.getLEFT_ROTATION(), transformation.getLeftRotation()),
                        new DataTracker.Entry<>(DisplayEntityAccessor.getRIGHT_ROTATION(), transformation.getRightRotation())
                )
        ));
    }
}
