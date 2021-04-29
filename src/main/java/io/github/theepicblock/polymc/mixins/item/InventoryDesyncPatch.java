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

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft checks to see if the inventories are out of sync.
 * But when using PolyMC a desync is intentional.
 * So we check here if the desync is actually a good desync or a bad desync.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class InventoryDesyncPatch {

    @Shadow public ServerPlayerEntity player;

    @Redirect(method = "onClickSlot(Lnet/minecraft/network/packet/c2s/play/ClickSlotC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean areEqualRedirect(ItemStack left, ItemStack right) {
        return ItemStack.areEqual(left, PolyMapProvider.getPolyMap(this.player).getClientItem(right, player));
    }
}
