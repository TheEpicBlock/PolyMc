package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.item.ItemLocation;

public class ItemLocationStaticHack {
    public static ThreadLocal<ItemLocation> location = new ThreadLocal<>();
}
