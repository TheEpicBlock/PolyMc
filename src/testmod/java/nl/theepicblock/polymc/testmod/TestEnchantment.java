package nl.theepicblock.polymc.testmod;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public class TestEnchantment extends Enchantment {
    protected TestEnchantment() {
        super(Rarity.COMMON, ItemTags.BUTTONS, new EquipmentSlot[0]);
    }

    @Override
    public int getMinPower(int level) {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return true;
    }
}
