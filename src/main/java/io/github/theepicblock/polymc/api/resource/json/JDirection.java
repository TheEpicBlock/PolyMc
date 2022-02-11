package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;

public enum JDirection {
    @SerializedName(value = "down", alternate = "bottom")
    DOWN,
    @SerializedName("up")
    UP,
    @SerializedName("north")
    NORTH,
    @SerializedName("south")
    SOUTH,
    @SerializedName("west")
    WEST,
    @SerializedName("east")
    EAST,
}
