package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;

public class BlockBreakingUtil {
    public static Collection<EntityAttributeInstance> DISABLER_ATTRIBUTES = Collections.singleton(
            new EntityAttributeInstance(
                    EntityAttributes.PLAYER_BLOCK_BREAK_SPEED,
                    (update) -> {}
            )
    );

    static {
        DISABLER_ATTRIBUTES.iterator().next().addPersistentModifier(new EntityAttributeModifier("polymc", -1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static void sendBreakDisabler(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(
                player.getId(),
                DISABLER_ATTRIBUTES
        ));
    }
}
