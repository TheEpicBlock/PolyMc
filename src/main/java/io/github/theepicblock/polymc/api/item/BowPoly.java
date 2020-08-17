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

import com.google.gson.reflect.TypeToken;
import io.github.theepicblock.polymc.api.register.CustomModelDataManager;
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BowPoly extends PredicateBasedDamageableItem {
    public BowPoly(CustomModelDataManager registerManager, Item base) {
        super(registerManager, base, Items.BOW);
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        pack.copyItemModel(item);

        Identifier serverItemId = Registry.ITEM.getId(defaultServerItem.getItem());
        Identifier bowModelPath = new Identifier(serverItemId.getNamespace(), "item/"+serverItemId.getPath());

        //default shield model
        if (!pack.hasPendingItemModel(bowModelPath)) {
            JsonModel bowModel = pack.getOrDefaultPendingItemModel(serverItemId.getPath());

            Type displayMap = new TypeToken<Map<String,JsonModel.DisplayEntry>>() {}.getType();
            bowModel.display = pack.getGson().fromJson("{\"thirdperson_righthand\":{\"rotation\":[-80,260,-40],\"translation\":[-1,-2,2.5],\"scale\":[0.9,0.9,0.9]},\"thirdperson_lefthand\":{\"rotation\":[-80,-280,40],\"translation\":[-1,-2,2.5],\"scale\":[0.9,0.9,0.9]},\"firstperson_righthand\":{\"rotation\":[0,-90,25],\"translation\":[1.13,3.2,1.13],\"scale\":[0.68,0.68,0.68]},\"firstperson_lefthand\":{\"rotation\":[0,90,-25],\"translation\":[1.13,3.2,1.13],\"scale\":[0.68,0.68,0.68]}}", displayMap);

            Type overrideType = new TypeToken<List<JsonModel.Override>>() {}.getType();
            bowModel.overrides = pack.getGson().fromJson("[{\"predicate\":{\"pulling\":1},\"model\":\"item\\/bow_pulling_0\"},{\"predicate\":{\"pulling\":1,\"pull\":0.65},\"model\":\"item\\/bow_pulling_1\"},{\"predicate\":{\"pulling\":1,\"pull\":0.9},\"model\":\"item\\/bow_pulling_2\"}]", overrideType);
        }

        super.AddToResourcePack(item,pack);
    }
}
