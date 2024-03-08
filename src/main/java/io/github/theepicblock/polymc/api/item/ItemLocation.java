package io.github.theepicblock.polymc.api.item;

public enum ItemLocation {
    INVENTORY,
    EQUIPMENT,
    TRACKED_DATA,
    /**
     * Item is included inside of text. Namely, hover events might contain an item stack.
     * See "show_item" on <a href="https://minecraft.wiki/w/Raw_JSON_text_format">the minecraft wiki</a>
     * @see net.minecraft.text.HoverEvent
     */
    TEXT,
    /**
     * Used for compat with other mods, namely Polymer.
     * Used to indicate that the item should be serialized as if it were to eventually reach a creative inventory
     */
    CREATIVE
}
