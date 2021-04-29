package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;

public class Original2NbtPoly implements ItemPoly {
    /**
     * The identifier in which the original item is stored
     */
    public static final String ORIGINAL_IDENTIFIER = "PolyMcOriginal";

    @Override
    public ItemStack getClientItem(ItemStack input) {
        return appendNbtToNbt(input);
    }

    public static ItemStack appendNbtToNbt(ItemStack input) {
        ItemStack ret = input.copy();
        ret.putSubTag(ORIGINAL_IDENTIFIER, input.toTag(new CompoundTag()));
        return ret;
    }

    public static ItemStack reverse(ItemStack input) {
        if (input.getTag() == null || !input.getTag().contains(ORIGINAL_IDENTIFIER, NbtType.COMPOUND)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.fromTag(input.getTag().getCompound(ORIGINAL_IDENTIFIER));
    }

    @Override
    public void addToResourcePack(Item item, ResourcePackMaker pack) {}
}
