package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameEntityAccessor {
    @Accessor(value = "ITEM_STACK")
    static TrackedData<ItemStack> getItemStackTracker() {
        throw new IllegalStateException();
    }

    @Accessor(value = "ROTATION")
    static TrackedData<Integer> getRotationTracker() {
        throw new IllegalStateException();
    }
}
