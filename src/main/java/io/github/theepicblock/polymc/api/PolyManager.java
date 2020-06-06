package io.github.theepicblock.polymc.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

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

    public static ItemStack getClientItem(ItemStack serverItem) {
        if (serverItem.isEmpty()) {
            return serverItem;
        }
        return new ItemStack(Items.STICK, serverItem.getCount());
    }
}
