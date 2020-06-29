package io.github.theepicblock.polymc.api.register;

import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to generate a map of Polys for items and to prevent conflicts in them
 */
public class ItemPolyMapBuilder {
    private final CustomModelDataManager CMDManager;
    private final Map<Item, ItemPoly> map = new HashMap<>();

    public ItemPolyMapBuilder() {
        this.CMDManager = new CustomModelDataManager();
    }

    /**
     * Register a poly for an item
     * @param item item to associate poly with
     * @param poly poly to register
     */
    public void register(Item item, ItemPoly poly) {
        map.put(item, poly);
    }

    public CustomModelDataManager getCMDManager() {
        return CMDManager;
    }

    public Map<Item, ItemPoly> build() {
        return map;
    }
}
