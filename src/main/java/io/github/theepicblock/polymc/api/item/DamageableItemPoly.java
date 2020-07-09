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

import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;

public class DamageableItemPoly extends CustomModelDataPoly{
    private final static int FUNGUS_MAX_DAMAGE = Items.WARPED_FUNGUS_ON_A_STICK.getMaxDamage();
    private final int maxDamage;
    public DamageableItemPoly(CustomModelDataManager registerManager, Item base) {
        super(registerManager, base, Items.WARPED_FUNGUS_ON_A_STICK);
        maxDamage = base.getMaxDamage();
    }

    @Override
    public ItemStack getClientItem(ItemStack input) {
        ItemStack sup = super.getClientItem(input);
        int inputDamage = input.getDamage();
        int damage = (int)(((float)inputDamage/maxDamage)*FUNGUS_MAX_DAMAGE);
        if (damage == 0 && inputDamage > 0) damage = 1; //If the item is damaged in any way it should show up
        sup.setDamage(damage);
        return sup;
    }
}
