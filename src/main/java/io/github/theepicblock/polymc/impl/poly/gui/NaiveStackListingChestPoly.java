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

import io.github.theepicblock.polymc.api.gui.GuiPoly;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class NaiveStackListingChestPoly implements GuiPoly {
    @Override
    public ScreenHandler replaceScreenHandler(ScreenHandler base, ServerPlayerEntity player, int syncId) {
        return new NaiveStackListingScreenHandler(ScreenHandlerType.GENERIC_9X3, 9, 3, syncId, player.getInventory(), base);
    }

    public static class NaiveStackListingScreenHandler extends ScreenHandler {
        protected final ScreenHandler base;
        /**
         * Total amount of slots in this screen. Without the player inventory
         */
        protected final int totalSlots;
        /**
         * Total amount of slots that aren't present in {@link #base}
         */
        protected final int fakedSlots;

        protected NaiveStackListingScreenHandler(ScreenHandlerType<?> type, int width, int height, int syncId, PlayerInventory playerInventory, ScreenHandler base) {
            super(type, syncId);
            this.base = base;
            this.totalSlots = width*height;
            int fakedSlotsTemp = 0;

            List<Slot> baseSlots = GuiUtils.removePlayerSlots(base.slots);
            for (int y = 0; y < width; ++y) {
                for (int x = 0; x < height; ++x) {
                    int index = x + y * width;

                    Slot slot;
                    if (baseSlots.size() > index) {
                        slot = baseSlots.get(index);
                    } else {
                        slot = new StaticSlot(new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
                        fakedSlotsTemp++;
                    }
                    this.addSlot(slot);
                }
            }

            //Player inventory
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }

            //Player hotbar
            for (int hotbar = 0; hotbar < 9; ++hotbar) {
                this.addSlot(new Slot(playerInventory, hotbar, 8 + hotbar * 18, 142));
            }

            this.fakedSlots = fakedSlotsTemp;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return base.canUse(player);
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
            if (index > totalSlots) {
                index -= fakedSlots;
            }
            return base.quickMove(player, index);
        }
    }
}
