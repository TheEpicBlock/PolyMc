package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;

public class FancyPantsItemPoly extends DamageableItemPoly {
    private final int color;

    public FancyPantsItemPoly(PolyRegistry builder, ArmorItem base) {
        this(builder, base, getReplacementItem(base.getSlotType()));
    }

    public FancyPantsItemPoly(PolyRegistry registry, ArmorItem base, Item replacementItem) {
        super(registry.getSharedValues(CustomModelDataManager.KEY), base, replacementItem);

        ArmorMaterial material = base.getMaterial();
        int color;
        try {
            color = registry.getSharedValues(ArmorColorManager.KEY).getColorForMaterial(material);
        } catch (Throwable e) {
            PolyMc.LOGGER.warn("Error getting color for armor "+ base.getTranslationKey() + ": "+e);
            color = 0;
        }
        this.color = color;
    }

    /**
     * Get the correct replacement item for the given slot
     */
    public static Item getReplacementItem(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS -> Items.LEATHER_LEGGINGS;
            case FEET -> Items.LEATHER_BOOTS;
            default -> null;
        };
    }

    @Override
    protected void addCustomTagsToItem(ItemStack stack) {
        super.addCustomTagsToItem(stack);
        NbtCompound nbt = stack.getOrCreateSubNbt("display");
        nbt.putInt("color", this.color);
        stack.addHideFlag(ItemStack.TooltipSection.DYE);
    }

    public static void onFirstRegister(PolyRegistry registry) {
        // Ensure colors with a modded texture assigned aren't used by vanilla items
        var armorManager = registry.getSharedValues(ArmorColorManager.KEY);
        for (var leatherItem : new Item[]{Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS}) {
            registry.registerItemPoly(leatherItem, (stack, location) -> {
                var dyeableItem = (DyeableItem)stack.getItem();
                if (armorManager.hasColor(dyeableItem.getColor(stack))) {
                    var copy = stack.copy();
                    // There should only be a modded armor piece every other color value
                    dyeableItem.setColor(copy, dyeableItem.getColor(stack)-1);
                    return copy;
                }
                return stack;
            });
        }
    }
}
