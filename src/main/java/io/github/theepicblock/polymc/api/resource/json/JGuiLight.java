package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the lighting of a model in a gui
 *
 * @see JModel#getGuiLight()
 * @see JModel#setGuiLight(JGuiLight)
 */
public enum JGuiLight {
    /**
     * Block-like shading
     */
    @SerializedName("front")
    FRONT,
    /**
     * Item-like shading
     */
    @SerializedName("side")
    SIDE
}
