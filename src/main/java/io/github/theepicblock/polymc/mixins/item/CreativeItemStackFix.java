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
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When items are moved around by a creative mode player, the client just tells the server to set a stack to a specific item.
 * This means that if the client thinks it's holding a stick, it will instruct the server to set the slot to a stick.
 * Even if the stick is supposed to represent another item.
 *
 * My hacky solution:
 * When a packet is sent to void a slot. The item previously in there gets set in "polyMCrecentlyVoided".
 * Then when it tries to set a slot to an item. It first gets checked to see if the item it tries to set could be the poly of
 * We also check if the client tries to set a slot to its polyd version.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class CreativeItemStackFix {
    @Shadow public ServerPlayerEntity player;

    @Redirect(method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;getItemStack()Lnet/minecraft/item/ItemStack;"))
    public ItemStack creativemodeSetSlotRedirect(CreativeInventoryActionC2SPacket packet) {
        return PolyMapProvider.getPolyMap(player).reverseClientItem(packet.getItemStack());
    }
}
