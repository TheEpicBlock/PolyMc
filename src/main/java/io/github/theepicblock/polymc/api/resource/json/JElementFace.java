package io.github.theepicblock.polymc.api.resource.json;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JElementFace {
    private final double[] uv;
    private final String texture;
    private final JDirection cullface;
    private final int rotation;
    private final int tintindex;

    public JElementFace(double[] uv, String texture, JDirection cullface, int rotation, int tintindex) {
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

    public int rotation() {
        return rotation;
    }

    public int tintindex() {
        return tintindex;
    }
}
