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
package io.github.theepicblock.polymc.api.item;

import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemPoly extends DebugInfoProvider<Item> {
    /**
     * Transforms an ItemStack to its clientside version.
     *
     * It's recommended to use {@link io.github.theepicblock.polymc.api.PolyMap#getClientBlock(BlockState)} when available instead of this method.
     * @apiNote this method should never edit the incoming ItemStack. As that might have unspecified consequences for the actual serverside representation of the item.
     * @param input the original {@link ItemStack} that's used serverside.
     * @return The {@link ItemStack} that should be sent to the client.
     */
    ItemStack getClientItem(ItemStack input);

    /**
     * Callback to add all resources needed for this item to a resource pack.
     * @param item item this ItemPoly was registered to, for added context.
     * @param pack resource pack the assets should be added to.
     */
    void addToResourcePack(Item item, ResourcePackMaker pack);
}
