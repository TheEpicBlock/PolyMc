package io.github.theepicblock.polymc.api.register;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes a {@link PolyMap}
 * It contains all the utilities needed to manage the different Poly types.
 */
public class PolyMapBuilder {
    private final CustomModelDataManager CMDManager = new CustomModelDataManager();
    private final Map<Item, ItemPoly> itemMap = new HashMap<>();

    /**
     * Register a poly for an item
     * @param item item to associate poly with
     * @param poly poly to register
     */
    public void registerItem(Item item, ItemPoly poly) {
        itemMap.put(item, poly);
    }

    public CustomModelDataManager getCMDManager() {
        return CMDManager;
    }
    public PolyMap build() {
        return new PolyMap(itemMap);
    }
}
