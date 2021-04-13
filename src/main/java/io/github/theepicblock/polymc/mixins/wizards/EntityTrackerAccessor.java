package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public interface EntityTrackerAccessor {
    @Accessor
    void setId(int v);

    @Accessor
    void setTrackedValues(List<DataTracker.Entry<?>> v);
}
