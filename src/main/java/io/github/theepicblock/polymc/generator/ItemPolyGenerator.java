package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.RandomCmdPoly;
import io.github.theepicblock.polymc.api.register.ItemPolyMapBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

/**
 * Class to automatically generate ItemPolys for Items
 */
public class ItemPolyGenerator {
    /**
     * Automatically generates all {@link ItemPoly}s that are missing in the specified builder
     * @param builder builder to add the {@link ItemPoly}s to
     */
    public static void generateMissing(ItemPolyMapBuilder builder) {
        for (Item item : getItemRegistry()) {
            Identifier id = getItemRegistry().getId(item);
            if (id == null) continue;
            if (!Util.isVanilla(id) || item != Items.AIR) { //TODO remove this debug thingy
                //this is a modded item and should have a Poly
                addItemToBuilder(item,builder);
            }
        }
    }

    public static ItemPoly generatePoly(Item item, ItemPolyMapBuilder builder) {
        return new RandomCmdPoly(builder.getCMDManager());
    }

    private static void addItemToBuilder(Item item, ItemPolyMapBuilder builder) {
        builder.register(item, generatePoly(item,builder));
    }

    /**
     * @return the minecraft item registry
     */
    private static DefaultedRegistry<Item> getItemRegistry() {
        return Registry.ITEM;
    }
}
