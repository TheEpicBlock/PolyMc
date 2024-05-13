package nl.theepicblock.polymc.testmod;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public class TestEnchantment extends Enchantment {
    protected TestEnchantment() {
        super(Enchantment.properties(ItemTags.BUTTONS, 10, 4, Enchantment.leveledCost(1, 11), Enchantment.leveledCost(12, 11), 1, EquipmentSlot.MAINHAND));
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return true;
    }
}
