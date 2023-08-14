package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.math.Direction;

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
    EAST;

    public Direction toMojang() {
        return switch (this) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
        };
    }

    public static JDirection fromMojang(Direction in) {
        return switch (in) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
}
