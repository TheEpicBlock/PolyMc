package io.github.theepicblock.polymc.api.resource.json;

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
}
