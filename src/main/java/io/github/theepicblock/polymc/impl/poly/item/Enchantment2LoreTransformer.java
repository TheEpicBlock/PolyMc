package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Poly that ports the non-vanilla enchantments of an item to the lore tag, so it can be displayed properly by the vanilla client.
 * <p>
 * This method uses the built-in {@link net.minecraft.enchantment.Enchantment#getName(int)} method,
 * which allows it to be properly formatted for cursed enchantments and remain also compatible with mods
 * that add custom formatting.
 * @see #portEnchantmentsToLore(ItemStack)
 */
public class Enchantment2LoreTransformer implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack input) {
        return portEnchantmentsToLore(input);
    }

    public static ItemStack portEnchantmentsToLore(ItemStack input) {
        if (input.hasTag() && input.getTag().contains("Enchantments", 9)) {
            // checks if the enchantments aren't hidden
            if (Util.isSectionVisible(input, ItemStack.TooltipSection.ENCHANTMENTS)) {
                ItemStack stack = input.copy(); // we should copy the ItemStack to prevent accidental modifications to the original
                NbtList enchantments = stack.getEnchantments();

                for (var enchantmentTag : enchantments) {
                    if (enchantmentTag.getType() != 10) continue; // checks if this is a CompoundTag
                    NbtCompound compoundTag = (NbtCompound)enchantmentTag;

                    Identifier id = Identifier.tryParse(compoundTag.getString("id"));

                    if (!Util.isVanilla(id) && id != null) {
                        Registry.ENCHANTMENT.getOrEmpty(id).ifPresent((enchantment) -> {
                            Text name = enchantment.getName(compoundTag.getInt("lvl"));

                            NbtCompound displayTag = stack.getOrCreateSubTag("display");
                            if (!displayTag.contains("Lore")) {
                                displayTag.put("Lore", new NbtList());
                            }
                            // place the enchantment on the lore
                            displayTag.getList("Lore", 8).add(NbtString.of(Text.Serializer.toJson(name)));
                        });
                    }
                }
                return stack;
            }
        }
        return input;
    }
}
