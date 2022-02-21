package io.github.theepicblock.polymc.api.resource.json;

import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JElementRotation {
    private final double[] origin;
    private final String axis;
    private final float angle;
    private final boolean rescale;

    public JElementRotation(double[] origin, String axis, float angle, boolean rescale) {
        this.origin = origin;
        this.axis = axis;
        this.angle = angle;
        this.rescale = rescale;
    }

    public double[] origin() {
        return origin;
    }

    public String axis() {
        return axis;
    }

    public float angle() {
        return angle;
    }

    public boolean rescale() {
        return rescale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JElementRotation that = (JElementRotation)o;
        return Float.compare(that.angle, angle) == 0 && rescale == that.rescale && Arrays.equals(origin, that.origin) && Objects.equals(axis, that.axis);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(axis, angle, rescale);
        result = 31 * result + Arrays.hashCode(origin);
        return result;
    }
}
