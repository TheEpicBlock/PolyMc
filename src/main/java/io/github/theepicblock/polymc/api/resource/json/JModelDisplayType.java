package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;

public enum JModelDisplayType {
    @SerializedName("thirdperson_righthand")
    THIRDPERSON_RIGHTHAND,
    @SerializedName("thirdperson_lefthand")
    THIRDPERSON_LEFTHAND,
    @SerializedName("firstperson_righthand")
    FIRSTPERSON_RIGHTHAND,
    @SerializedName("firstperson_lefthand")
    FIRSTPERSON_LEFTHAND,
    @SerializedName("gui")
    GUI,
    @SerializedName("head")
    HEAD,
    @SerializedName("ground")
    GROUND,
    /**
     * Refers to itemframes
     */
    @SerializedName("fixed")
    FIXED
}
