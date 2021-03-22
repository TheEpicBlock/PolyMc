package io.github.theepicblock.polymc.mixins.wizards.block;

import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPositionS2CPacket.class)
public interface EntityPositionPacketAccessor {
    @Accessor
    void setId(int i);
    @Accessor("x")
    void setX(double i);
    @Accessor("y")
    void setY(double i);
    @Accessor("z")
    void setZ(double i);
    @Accessor
    void setYaw(byte i);
    @Accessor
    void setPitch(byte i);
    @Accessor
    void setOnGround(boolean b);
}
