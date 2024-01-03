package io.github.theepicblock.polymc.api.item;

public enum ItemLocation {
    INVENTORY,
    EQUIPMENT,
    TRACKED_DATA,
    /**
     * Used for compat with other mods, namely Polymer.
     * Used to indicate that the item should be serialized as if it were to eventually reach a creative inventory
     */
    CREATIVE
}
