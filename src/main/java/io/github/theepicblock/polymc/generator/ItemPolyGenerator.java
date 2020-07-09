package io.github.theepicblock.polymc.generator;

import io.github.theepicblock.polymc.Util;
import io.github.theepicblock.polymc.api.item.DamageableItemPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.CustomModelDataPoly;
import io.github.theepicblock.polymc.api.register.PolyRegister;
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
    public static void generateMissing(PolyRegister builder) {
        for (Item item : getItemRegistry()) {
            Identifier id = getItemRegistry().getId(item);
            if (!Util.isVanilla(id)) {
                //this is a modded item and should have a Poly
                addItemToBuilder(item,builder);
            }
        }
    }

    /**
     * Generates the most suitable ItemPoly for a given item
     */
    public static ItemPoly generatePoly(Item item, PolyRegister builder) {
        if (item.isDamageable()) {
            return new DamageableItemPoly(builder.getCMDManager(), item);
        }

        if (item.isFood()) {
            return new CustomModelDataPoly(builder.getCMDManager(), item, Items.COOKED_BEEF);
        }
        return new CustomModelDataPoly(builder.getCMDManager(), item);
    }

    /**
     * Generates the most suitable ItemPoly and directly adds it to the {@link PolyRegister}
     * @see #generatePoly(Item, PolyRegister)
     */
    private static void addItemToBuilder(Item item, PolyRegister builder) {
        builder.registerItem(item, generatePoly(item,builder));
    }

    /**
     * @return the minecraft item registry
     */
    private static DefaultedRegistry<Item> getItemRegistry() {
        return Registry.ITEM;
    }
}
