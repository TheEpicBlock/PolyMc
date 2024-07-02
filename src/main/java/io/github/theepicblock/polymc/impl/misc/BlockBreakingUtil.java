package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

public class BlockBreakingUtil {

    public static Identifier POLYMC_MODIFIER_ID = Identifier.of("polymc", "block_breaking");

    public static Collection<EntityAttributeInstance> DISABLER_ATTRIBUTES = Collections.singleton(
            new EntityAttributeInstance(
                    EntityAttributes.PLAYER_BLOCK_BREAK_SPEED,
                    (update) -> {}
            )
    );

    public static Collection<EntityAttributeInstance> REMOVE_DISABLER_ATTRIBUTES = Collections.singleton(
            new EntityAttributeInstance(
                    EntityAttributes.PLAYER_BLOCK_BREAK_SPEED,
                    (update) -> {}
            )
    );

    static {
        DISABLER_ATTRIBUTES.iterator().next().addPersistentModifier(new EntityAttributeModifier(POLYMC_MODIFIER_ID, -1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        REMOVE_DISABLER_ATTRIBUTES.iterator().next().removeModifier(POLYMC_MODIFIER_ID);
    }

    public static void sendBreakDisabler(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(
                player.getId(),
                DISABLER_ATTRIBUTES
        ));
    }

    public static void removeBreakDisabler(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(
                player.getId(),
                REMOVE_DISABLER_ATTRIBUTES
        ));
    }
}
