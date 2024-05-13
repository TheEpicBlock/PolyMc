package io.github.theepicblock.polymc.api.resource.json;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JElementFace {
    private final double[] uv;
    private final String texture;
    private final JDirection cullface;
    private final Integer rotation;
    private final Integer tintindex;

    public JElementFace(double[] uv, String texture, JDirection cullface, @Nullable Integer rotation, @Nullable Integer tintindex) {
        this.uv = uv;
        this.texture = texture;
        this.cullface = cullface;
        this.rotation = rotation;
        this.tintindex = tintindex;
    }

    public double[] uv() {
        return uv;
    }

    public String texture() {
        return texture;
    }

    public JDirection cullface() {
        return cullface;
    }

    public Integer rotation() {
        return rotation;
    }

    public Integer tintindex() {
        return tintindex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JElementFace that = (JElementFace)o;
        return (rotation == null || that.rotation == null) ? rotation == that.rotation : rotation.intValue() == that.rotation.intValue() &&
                (tintindex == null || that.tintindex == null) ? tintindex == that.tintindex : tintindex.intValue() == that.tintindex.intValue() &&
                Arrays.equals(uv, that.uv) && Objects.equals(texture, that.texture) && cullface == that.cullface;
    }

    @Override
    public int hashCode() {
        int result;
        if (tintindex != null) {
            if (rotation != null) result = Objects.hash(texture, cullface, rotation, tintindex);
            else result = Objects.hash(texture, cullface, tintindex);
        }
        else {
            if (rotation != null) result = Objects.hash(texture, cullface, rotation);
            else result = Objects.hash(texture, cullface);
        }
        result = 31 * result + Arrays.hashCode(uv);
        return result;
    }
}
