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
