package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

/**
 *
 */
public class PolyMap {
    private final Map<Item, ItemPoly> itemPolys;

    public PolyMap(Map<Item, ItemPoly> itemPolys) {
        this.itemPolys = itemPolys;
    }

    public ItemStack getClientItem(ItemStack serverItem) {
        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly == null) return serverItem;

        return poly.getClientItem(serverItem);
    }

    public Map<Item, ItemPoly> getItemPolys() {
        return itemPolys;
    }
}
