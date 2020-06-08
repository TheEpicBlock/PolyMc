package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;

public class PolyMap {
    private final Map<Item, ItemPoly> itemPolys;

    public PolyMap(Map<Item, ItemPoly> itemPolys) {
        this.itemPolys = itemPolys;
    }

    public ItemStack getClientItem(ItemStack serverItem) {
        if (serverItem.isEmpty()) {
            return serverItem;
        }
        return new ItemStack(Items.STICK, serverItem.getCount());
    }
}
