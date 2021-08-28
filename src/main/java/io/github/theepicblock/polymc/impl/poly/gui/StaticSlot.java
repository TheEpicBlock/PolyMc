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
package io.github.theepicblock.polymc.impl.poly.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class StaticSlot extends Slot {
    public final ItemStack stack;

    public StaticSlot(ItemStack stack) {
        super(EmptyInventory.INSTANCE, 0, 0, 0);
        this.stack = stack;
    }

    public void onQuickTransfer(ItemStack originalItem, ItemStack itemStack) {
        throw new AssertionError("PolyMc: the contents of a static, unchangeable slot were changed. Containing: " + stack.toString());
    }

    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        throw new AssertionError("PolyMc: tried to take item out of an static, unchangeable slot. Containing: " + stack.toString());
    }

    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    public boolean canInsert(ItemStack stack) {
        return false;
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        GuiUtils.resyncPlayerInventory(playerEntity);
        return false;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setStack(ItemStack stack) {
    }

    public void markDirty() {
    }

    public int getMaxItemCount() {
        return this.stack.getCount();
    }

    public int getMaxItemCount(ItemStack stack) {
        return this.getMaxItemCount();
    }
}
