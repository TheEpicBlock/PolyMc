package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.api.PolyMapBuilder;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.Iterator;

/**
 * Utility class to generate {@link ItemPoly}s for items and to add them to a {@link PolyMapBuilder}
 */
public class ItemGenerator {
    private final CustomModelDataManager registerManager;

    public ItemGenerator() {
        this.registerManager = new CustomModelDataManager();
    }

    /**
     * Makes sure that all items that are in the item registry have valid {@link ItemPoly}s.
     * @param builder builder to add the {@link ItemPoly}s to
     */
    public static void generateAllItems(PolyMapBuilder builder) {

    }

    /**
     * @return an {@link Iterator} of {@link Registry#ITEM}
     */
    public static Iterator<Item> getItemIterator() {
        return Registry.ITEM.iterator();
    }
}
