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

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ItemLocationStaticHack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * This is the class responsible for replacing the serverside items with the clientside items
 */
@Mixin(PacketByteBuf.class)
public class ItemPolyImplementation {
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
    public ItemStack writeItemStackHook(ItemStack itemStack) {
        ServerPlayerEntity player = PacketContext.get().getTarget();
        var map = Util.tryGetPolyMap(player);
        return map.getClientItem(itemStack, player, ItemLocationStaticHack.location.get());
    }
}