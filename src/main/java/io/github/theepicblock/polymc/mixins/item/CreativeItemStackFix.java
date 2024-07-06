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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * When items are moved around by a creative mode player, the client just tells the server to set a stack to a specific item.
 * This means that if the client thinks it's holding a stick, it will instruct the server to set the slot to a stick.
 * Even if the stick is supposed to represent another item. To fix this, we store the original full itemstack inside
 * the polyd itemstack and restore it when we receive the packet.
 * @see io.github.theepicblock.polymc.impl.PolyMapImpl#getClientItem(ItemStack, ServerPlayerEntity, ItemLocation)
 * @see io.github.theepicblock.polymc.api.PolyMap#reverseClientItem(ItemStack)
 */
@Mixin(ServerPlayNetworkHandler.class)
public class CreativeItemStackFix {
    @Shadow public ServerPlayerEntity player;

    @ModifyExpressionValue(method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;stack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack creativemodeSetSlotRedirect(ItemStack original) {
        return PolyMapProvider.getPolyMap(player).reverseClientItem(original, player);
    }
}
