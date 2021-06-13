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
package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

/**
 * To prevent this factory being detected by some of the instanceof's Fabric Api does.
 * This prevents the Fabric api from trying to include extra data.
 * @see net.fabricmc.fabric.mixin.screenhandler.ServerPlayerEntityMixin
 */
public class ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt implements NamedScreenHandlerFactory {
    private final NamedScreenHandlerFactory inner;

    public ScreenHandlerFactoryWrapperSoFabricApiDoesntDetectIt(NamedScreenHandlerFactory inner) {
        this.inner = inner;
    }

    @Override
    public Text getDisplayName() {
        return inner.getDisplayName();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return inner.createMenu(syncId, inv, player);
    }
}
