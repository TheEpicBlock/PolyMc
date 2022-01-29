package io.github.theepicblock.polymc.api.resource.json;

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
}
