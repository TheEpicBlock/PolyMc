package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.class)
public interface DisplayEntityAccessor {
    @Accessor
    static TrackedData<Quaternionf> getRIGHT_ROTATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Byte> getBILLBOARD() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getBRIGHTNESS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getVIEW_RANGE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getSHADOW_RADIUS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getSHADOW_STRENGTH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getWIDTH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Float> getHEIGHT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getGLOW_COLOR_OVERRIDE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Vector3f> getTRANSLATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Vector3f> getSCALE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Quaternionf> getLEFT_ROTATION() {
        throw new UnsupportedOperationException();
    }
}
