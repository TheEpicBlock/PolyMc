package io.github.theepicblock.polymc.mixins.item;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface EntityAttributeUuidAccessor {
    @Accessor
    static UUID getATTACK_DAMAGE_MODIFIER_ID() {
        throw new IllegalStateException();
    }

    @Accessor
    static UUID getATTACK_SPEED_MODIFIER_ID() {
        throw new IllegalStateException();
    }

}
