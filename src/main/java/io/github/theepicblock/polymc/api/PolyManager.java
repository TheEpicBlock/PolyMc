package io.github.theepicblock.polymc.api;

import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class PolyManager {
    private static final Map<Item, ItemPoly> itemPolys= new HashMap<>();

    /**
     * Registers an item poly
     * @param item item to make this poly work on
     * @param poly poly to use
     */
    public static void registerItem(Item item, ItemPoly poly) {
        itemPolys.put(item, poly);
    }
}
