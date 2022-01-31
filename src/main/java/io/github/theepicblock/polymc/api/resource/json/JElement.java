package io.github.theepicblock.polymc.api.resource.json;

import net.minecraft.util.math.Direction;

import java.util.Map;

public final class JElement {
    private final double[] from;
    private final double[] to;
    private final JElementRotation rotation;
    private final boolean shade;
    private final Map<Direction, JElementFace> faces;

    public JElement(double[] from, double[] to, JElementRotation rotation, boolean shade, Map<Direction,JElementFace> faces) {
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

    public Map<Direction,JElementFace> faces() {
        return faces;
    }
}
