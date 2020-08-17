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
import io.github.theepicblock.polymc.resource.JsonModel;
import io.github.theepicblock.polymc.resource.ResourcePackMaker;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * For items which render based on a predicate. It will also do damage calculations
 * It will copy predicate based models from the original item model into the resourcepack
 * This might need to be merged into {@link CustomModelDataPoly} at some point. Idk
 */
public class PredicateBasedDamageableItem extends DamageableItemPoly {
    public PredicateBasedDamageableItem(CustomModelDataManager registerManager, Item base) {
        super(registerManager, base);
    }

    public PredicateBasedDamageableItem(CustomModelDataManager registerManager, Item base, Item target) {
        super(registerManager, base, target);
    }

    @Override
    public void AddToResourcePack(Item item, ResourcePackMaker pack) {
        JsonModel clientModelJson = pack.getOrDefaultPendingItemModel(Registry.ITEM.getId(defaultServerItem.getItem()).getPath());
        Identifier serverModelId = Registry.ITEM.getId(item);

        InputStreamReader serverModelReader = pack.getAsset(serverModelId.getNamespace(),"models/item/"+serverModelId.getPath()+".json");
        JsonModel serverModel = pack.getGson().fromJson(serverModelReader,JsonModel.class);

        JsonModel.Override defaultCMDOverride = new JsonModel.Override();
        defaultCMDOverride.predicate = new HashMap<>();
        defaultCMDOverride.predicate.put("custom_model_data",(double)CMDvalue);
        defaultCMDOverride.model = serverModelId.getNamespace()+":item/"+serverModelId.getPath();
        clientModelJson.addOverride(defaultCMDOverride);

        for (JsonModel.Override override : serverModel.overrides) {
            JsonModel.Override newOverride = new JsonModel.Override();
            if (override.predicate == null) {
                newOverride.predicate = new HashMap<>();
            } else {
                newOverride.predicate = override.predicate;
            }
            newOverride.predicate.put("custom_model_data",(double)CMDvalue);
            newOverride.model = override.model;
            pack.copyModel(new Identifier(override.model));
            clientModelJson.addOverride(newOverride);
        }
    }
}
