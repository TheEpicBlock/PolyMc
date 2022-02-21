package io.github.theepicblock.polymc.api.resource.json;

import java.util.Arrays;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JModelDisplay {
    private final double[] rotation;
    private final double[] translation;
    private final double[] scale;

    public JModelDisplay(double[] rotation, double[] translation, double[] scale) {
        this.rotation = rotation;
        this.translation = translation;
        this.scale = scale;
    }

    public double[] rotation() {
        return rotation;
    }

    public double[] translation() {
        return translation;
    }

    public double[] scale() {
        return scale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JModelDisplay that = (JModelDisplay)o;
        return Arrays.equals(rotation, that.rotation) && Arrays.equals(translation, that.translation) && Arrays.equals(scale, that.scale);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(rotation);
        result = 31 * result + Arrays.hashCode(translation);
        result = 31 * result + Arrays.hashCode(scale);
        return result;
    }
}
