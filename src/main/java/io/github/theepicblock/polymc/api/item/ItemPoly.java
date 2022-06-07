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
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface ItemPoly extends DebugInfoProvider<Item> {
    /**
     * Transforms an ItemStack to its clientside version.
     * <p>
     * @param input the original {@link ItemStack} that's used serverside.
     * @param location the place this item is being sent from
     * @return The {@link ItemStack} that should be sent to the client.
     * @apiNote this method should never edit the incoming ItemStack. As that might have unspecified consequences for the actual serverside representation of the item.
     */
    ItemStack getClientItem(ItemStack input, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location);

    /**
     * Callback to add all resources needed for this item to a resource pack.
     * @param item item this ItemPoly was registered to, for added context.
     * @param moddedResources a container to retrieve modded assets from.
     * @param pack resource pack the assets should be added to.
     * @param logger a logger for this session. Will output to the person generating the assets
     */
    default void addToResourcePack(Item item, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {

    }
}
