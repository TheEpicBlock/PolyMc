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
package io.github.theepicblock.polymc.mixins.gui;

import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class GuiPolyImplementation {

    @Redirect(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/NamedScreenHandlerFactory;createMenu(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/screen/ScreenHandler;"))
    public ScreenHandler handlerId(NamedScreenHandlerFactory namedScreenHandlerFactory, int syncId, PlayerInventory inv, PlayerEntity player) {
        ScreenHandler base = namedScreenHandlerFactory.createMenu(syncId, inv, player);
        if (base == null) return null;

        GuiPoly poly = PolyMapProvider.getPolyMap((ServerPlayerEntity)player).getGuiPoly(base.getType());
        if (poly != null) {
            return poly.replaceScreenHandler(base, (ServerPlayerEntity)player, syncId);
        } else {
            return base;
        }
    }

    @ModifyVariable(method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;", at = @At("HEAD"), argsOnly = true)
    private NamedScreenHandlerFactory hackForFabricApi(NamedScreenHandlerFactory factory) {
        if (Util.isPolyMapVanillaLike((ServerPlayerEntity)(Object)this) && factory instanceof ExtendedScreenHandlerFactory) {
            return new ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt(factory);
        }
        return factory;
    }
}
