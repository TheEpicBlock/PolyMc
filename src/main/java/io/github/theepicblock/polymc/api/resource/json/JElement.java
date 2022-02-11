package io.github.theepicblock.polymc.api.resource.json;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public final class JElement {
    private final double[] from;
    private final double[] to;
    private final JElementRotation rotation;
    private final boolean shade;
    private final Map<JDirection, JElementFace> faces;

    public JElement(double[] from, double[] to, JElementRotation rotation, boolean shade, Map<JDirection,JElementFace> faces) {
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.shade = shade;
        this.faces = faces;
    }

    public double[] from() {
        return from;
    }

    public double[] to() {
        return to;
    }

    public JElementRotation rotation() {
        return rotation;
    }

    public boolean shade() {
        return shade;
    }

    public Map<JDirection,JElementFace> faces() {
        return faces;
    }
}
