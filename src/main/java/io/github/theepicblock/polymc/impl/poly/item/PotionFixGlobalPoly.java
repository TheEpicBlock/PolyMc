package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;

public class PotionFixGlobalPoly implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap map, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        var i = input.getItem();
        if (i == Items.POTION || i == Items.LINGERING_POTION || i == Items.SPLASH_POTION || i == Items.TIPPED_ARROW) {
            var ret = input.copy();
            var color = PotionUtil.getColor(ret);
            ret.getOrCreateNbt().putInt(PotionUtil.CUSTOM_POTION_COLOR_KEY, color);

            if (!input.hasCustomName()) {
                // Set the name to what the server thinks the name is
                var name = input.getName();

                if (location != ItemLocation.TRACKED_DATA) {
                    // Override the style to make sure the client does not render
                    // the custom name in italics, and uses the correct rarity format
                    if (name instanceof MutableText mutableText) {
                        mutableText.setStyle(name.getStyle().withItalic(false).withColor(input.getRarity().formatting));
                    }

                    ret.setCustomName(name);
                }
            }

            return ret;
        }

        return input;
    }
}
