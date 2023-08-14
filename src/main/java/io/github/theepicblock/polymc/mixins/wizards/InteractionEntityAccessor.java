package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.InteractionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InteractionEntity.class)
public interface InteractionEntityAccessor {
    @Accessor
    static TrackedData<Float> getWIDTH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getHEIGHT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getRESPONSE() {
        throw new UnsupportedOperationException();
    }
}
