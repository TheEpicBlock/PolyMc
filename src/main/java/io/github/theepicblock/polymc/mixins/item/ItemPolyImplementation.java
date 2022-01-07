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
package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This is the class responsible for replacing the serverside items with the clientside items
 */
@Mixin(PacketByteBuf.class)
public class ItemPolyImplementation {
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
    public ItemStack writeItemStackHook(ItemStack itemStack) {
        ServerPlayerEntity player = PlayerContextContainer.retrieve(this);
        if (player == null) return PolyMc.getMainMap().getClientItem(itemStack, null);
        return PolyMapProvider.getPolyMap(player).getClientItem(itemStack, player);
    }

    @Redirect(method = {"writeItemStack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getRawId(Lnet/minecraft/item/Item;)I"))
    public int getIdRedirect(Item item) {
        ServerPlayerEntity player = PlayerContextContainer.retrieve(this);
        PolyMap map = player == null ? PolyMc.getMainMap() : PolyMapProvider.getPolyMap(player);
        return map.getClientItemRawId(item, player);
    }

    @Redirect(method = {"readItemStack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;byRawId(I)Lnet/minecraft/item/Item;"))
    public Item reverseClientItemId(int rawClientSideItemId) {
        ServerPlayerEntity player = PlayerContextContainer.retrieve(this);
        PolyMap map = player == null ? PolyMc.getMainMap() : PolyMapProvider.getPolyMap(player);
        return map.reverseClientItemRawId(rawClientSideItemId, player);
    }
}