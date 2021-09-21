/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * This is the standard implementation of the PolyMap that PolyMc uses by default.
 * You can use a {@link io.github.theepicblock.polymc.api.PolyRegistry} to build one of these more easily.
 */
public class PolyMapImpl implements PolyMap {
    /**
     * The nbt tag name that stores the original item nbt so it can be restored
     * @see PolyMap#getClientItem(ItemStack, ServerPlayerEntity)
     * @see #recoverOriginalItem(ItemStack)
     */
    private static final String ORIGINAL_ITEM_NBT = "PolyMcOriginal";

    private final ImmutableMap<Item,ItemPoly> itemPolys;
    private final ItemTransformer[] globalItemPolys;
    private final ImmutableMap<Block,BlockPoly> blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys;
    private final ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys;

    public PolyMapImpl(ImmutableMap<Item,ItemPoly> itemPolys, ItemTransformer[] globalItemPolys, ImmutableMap<Block,BlockPoly> blockPolys, ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys, ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys) {
        this.itemPolys = itemPolys;
        this.globalItemPolys = globalItemPolys;
        this.blockPolys = blockPolys;
        this.guiPolys = guiPolys;
        this.entityPolys = entityPolys;
    }

    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayerEntity player) {
        ItemStack ret = serverItem;
        NbtCompound originalNbt = serverItem.writeNbt(new NbtCompound());

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem);

        for (ItemTransformer globalPoly : globalItemPolys) {
            ret = globalPoly.transform(ret);
        }

        if ((player == null || player.isCreative()) && !ItemStack.canCombine(serverItem, ret) && !serverItem.isEmpty()) {
            // Preserves the nbt of the original item so it can be reverted
            ret = ret.copy();
            ret.setSubNbt(ORIGINAL_ITEM_NBT, originalNbt);
        }

        return ret;
    }

    @Override
    public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
        return guiPolys.get(serverGuiType);
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return blockPolys.get(block);
    }

    @Override
    public ImmutableMap<Item,ItemPoly> getItemPolys() {
        return itemPolys;
    }

    @Override
    public ImmutableMap<Block,BlockPoly> getBlockPolys() {
        return blockPolys;
    }

    @Override
    public <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity) {
        return (EntityPoly<T>)entityPolys.get(entity);
    }

    @Override
    public ItemStack reverseClientItem(ItemStack clientItem) {
        return recoverOriginalItem(clientItem);
    }

    public static ItemStack recoverOriginalItem(ItemStack input) {
        if (input.getNbt() == null || !input.getNbt().contains(ORIGINAL_ITEM_NBT, NbtType.COMPOUND)) {
            return input;
        }

        NbtCompound tag = input.getNbt().getCompound(ORIGINAL_ITEM_NBT);
        ItemStack stack = ItemStack.fromNbt(tag);
        stack.setCount(input.getCount()); // The clientside count is leading, to support middle mouse button duplication and stack splitting and such

        if (stack.isEmpty() && !input.isEmpty()) {
            stack = new ItemStack(Items.CLAY_BALL);
            stack.setCustomName(new LiteralText("Invalid Item").formatted(Formatting.ITALIC));
        }
        return stack;
    }

    @Override
    public boolean isVanillaLikeMap() {
        return true;
    }
}
