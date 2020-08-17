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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShieldPoly extends PredicateBasedDamageableItem {
    public ShieldPoly(CustomModelDataManager registerManager, Item base) {
        super(registerManager, base, Items.SHIELD);
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        pack.copyItemModel(item);

        Identifier serverItemId = Registry.ITEM.getId(defaultServerItem.getItem());
        Identifier shieldModelPath = new Identifier(serverItemId.getNamespace(),"item/"+serverItemId.getPath());

        //default shield model
        if (!pack.hasPendingModel(shieldModelPath)) {
            JsonModel shieldModel = new JsonModel();
            shieldModel.parent = "builtin/entity";
            shieldModel.gui_light="front";
            shieldModel.textures = new HashMap<>();
            shieldModel.textures.put("particle", "block/dark_oak_planks");
            Type displayMap = new TypeToken<Map<String, JsonModel.DisplayEntry>>() {}.getType();
            shieldModel.display = pack.getGson().fromJson("{\"thirdperson_righthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,-4],\"scale\":[1,1,1]},\"thirdperson_lefthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,12],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,180,5],\"translation\":[-10,2,-10],\"scale\":[1.25,1.25,1.25]},\"firstperson_lefthand\":{\"rotation\":[0,180,5],\"translation\":[10,0,-10],\"scale\":[1.25,1.25,1.25]},\"gui\":{\"rotation\":[15,-25,-5],\"translation\":[2,3,0],\"scale\":[0.65,0.65,0.65]},\"fixed\":{\"rotation\":[0,180,0],\"translation\":[-2,4,-5],\"scale\":[0.5,0.5,0.5]},\"ground\":{\"rotation\":[0,0,0],\"translation\":[4,4,2],\"scale\":[0.25,0.25,0.25]}}", displayMap);

            //add blocking override
            shieldModel.overrides = new ArrayList<>();
            JsonModel.Override override = new JsonModel.Override();
            override.predicate = new HashMap<>();
            override.predicate.put("blocking",(double)1);
            override.model = "minecraft:item/shield_blocking";
            shieldModel.addOverride(override);
            pack.putPendingModel(shieldModelPath,shieldModel);
        }

        super.AddToResourcePack(item,pack);
    }
}
