package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import net.minecraft.item.Item;

public class PlaceableItemPoly extends CustomModelDataPoly {
    public PlaceableItemPoly(CustomModelDataManager registerManager, Item moddedBase) {
        super(registerManager, moddedBase);
    }

    public PlaceableItemPoly(CustomModelDataManager registerManager, Item moddedBase, Item target) {
        super(registerManager, moddedBase, target);
    }

    public PlaceableItemPoly(CustomModelDataManager registerManager, Item moddedBase, Item[] targets) {
        super(registerManager, moddedBase, targets);
    }
}
