package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains all the utilities to make a {@link PolyMap}
 */
public class PolyMapBuilder {
    private final Map<Item, ItemPoly> itemPolys = new HashMap<>();
    public final CustomModelDataManager customModelDataManager = new CustomModelDataManager();

    public void registerItemPoly(Item item, ItemPoly poly) {
        itemPolys.put(item,poly);
    }

    public PolyMap build() {
        return new PolyMap(itemPolys);
    }
}
