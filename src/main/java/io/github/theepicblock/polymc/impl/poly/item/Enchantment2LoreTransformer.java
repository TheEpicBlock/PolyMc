package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

/**
 * Poly that ports the non-vanilla enchantments of an item to the lore tag, so it can be displayed properly by the vanilla client.
 * <p>
 * This method uses the built-in {@link net.minecraft.enchantment.Enchantment#getName(int)} method,
 * which allows it to be properly formatted for cursed enchantments and remain also compatible with mods
 * that add custom formatting.
 * @see #portEnchantmentsToLore(ItemStack, boolean)
 */
public class Enchantment2LoreTransformer implements ItemTransformer {
    @Override
    public ItemStack transform(ItemStack original, ItemStack input, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        var map = Util.tryGetPolyMap(player);

        return portEnchantmentsToLore(input, map.getItemPoly(original.getItem()) != null);
    }

    public static ItemStack portEnchantmentsToLore(ItemStack input, boolean atBeginning) {
        if (input.getNbt() == null) return input;

        if (input.getItem() == Items.ENCHANTED_BOOK) {
            if (Util.isSectionVisible(input, ItemStack.TooltipSection.ENCHANTMENTS)) {
                var enchantments = EnchantedBookItem.getEnchantmentNbt(input);
                return processEnchantments(enchantments, input, atBeginning);
            }
        } else {
            if (Util.isSectionVisible(input, ItemStack.TooltipSection.ENCHANTMENTS) && input.getNbt().contains(ItemStack.ENCHANTMENTS_KEY, 9)) {
                var enchantments = input.getEnchantments();
                return processEnchantments(enchantments, input, atBeginning);
            }
        }

        return input;
    }

    private static ItemStack processEnchantments(NbtList enchantments, ItemStack input, boolean atBeginning) {
        ItemStack stack = input.copy(); // we should copy the ItemStack to prevent accidental modifications to the original

        var displayTag = stack.getOrCreateSubNbt("display");
        if (!displayTag.contains("Lore")) {
            displayTag.put("Lore", new NbtList());
        }

        for (var enchantmentTag : enchantments) {
            var enchantmentCompound = (NbtCompound)enchantmentTag;

            var id = EnchantmentHelper.getIdFromNbt(enchantmentCompound);
            if (!Util.isVanilla(id)) {
                Registry.ENCHANTMENT.getOrEmpty(id).ifPresent((enchantment) -> {
                    var name = enchantment.getName(EnchantmentHelper.getLevelFromNbt(enchantmentCompound)).copy();
                    name.setStyle(name.getStyle().withItalic(name.getStyle().isItalic()));
                    if (atBeginning) {
                        displayTag.getList("Lore", NbtElement.STRING_TYPE).add(0, NbtString.of(Text.Serializer.toJson(name)));
                    } else {
                        displayTag.getList("Lore", NbtElement.STRING_TYPE).add(NbtString.of(Text.Serializer.toJson(name)));
                    }
                });
            }
        }

        return stack;
    }
}
