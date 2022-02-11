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
package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class DamageableItemPoly extends CustomModelDataPoly {
    private final int clientSideMaxDamage;
    private final int serverSideMaxDamage;

    public DamageableItemPoly(CustomModelDataManager registerManager, Item base) {
        this(registerManager, base, Items.WARPED_FUNGUS_ON_A_STICK);
    }

    public DamageableItemPoly(CustomModelDataManager registerManager, Item base, Item target) {
        super(registerManager, base, target);
        clientSideMaxDamage = target.getMaxDamage();
        serverSideMaxDamage = base.getMaxDamage();
    }

    @Override
    public ItemStack getClientItem(ItemStack input, @Nullable ItemLocation location) {
        ItemStack sup = super.getClientItem(input, location);
        int inputDamage = input.getDamage();
        int damage = (int)(((float)inputDamage / serverSideMaxDamage) * clientSideMaxDamage); //convert serverside damage to clientside damage
        if (damage == 0 && inputDamage > 0) damage = 1; //If the item is damaged in any way it should show up
        sup.setDamage(damage);

        return sup;
    }
}
