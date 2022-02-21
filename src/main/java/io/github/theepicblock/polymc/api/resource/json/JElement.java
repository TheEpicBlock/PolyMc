package io.github.theepicblock.polymc.api.resource.json;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JElement jElement = (JElement)o;
        return shade == jElement.shade && Arrays.equals(from, jElement.from) && Arrays.equals(to, jElement.to) && Objects.equals(rotation, jElement.rotation) && Objects.equals(faces, jElement.faces);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rotation, shade, faces);
        result = 31 * result + Arrays.hashCode(from);
        result = 31 * result + Arrays.hashCode(to);
        return result;
    }
}
