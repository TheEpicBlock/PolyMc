package io.github.theepicblock.polymc.impl.poly.item;

import com.google.common.collect.Multimap;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FancyPantsItemPoly extends DamageableItemPoly {
    private final EquipmentSlot slot;
    private final int color;

    public FancyPantsItemPoly(PolyRegistry builder, ArmorItem base) {
        this(builder, base, getReplacementItem(base.getSlotType()));
    }

    public FancyPantsItemPoly(PolyRegistry registry, ArmorItem base, Item replacementItem) {
        super(registry.getSharedValues(CustomModelDataManager.KEY), base, replacementItem);

        ArmorMaterial material = base.getMaterial();
        this.slot = base.getSlotType();
        this.color = registry.getSharedValues(ArmorColorManager.KEY).getColorForMaterial(material);
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
    public ItemStack getClientItem(ItemStack input, @Nullable ItemLocation location) {
        ItemStack output = super.getClientItem(input, location);

        NbtCompound nbt = output.getOrCreateSubNbt("display");
        nbt.putInt("color", this.color);

        // Get the armor attribute modifiers, so the correct stats are shown on the tooltip
        try {
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = input.getItem().getAttributeModifiers(slot);

            if (!multimap.isEmpty()) {
                NbtList list = new NbtList();

                // Iterate over the key & values of the multimap
                for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                    EntityAttributeModifier modifier = entry.getValue();
                    EntityAttribute attribute = entry.getKey();

                    NbtCompound modifierNbt = modifier.toNbt();
                    modifierNbt.putString("AttributeName", Registry.ATTRIBUTE.getId(attribute).toString());

                    if (slot != null) {
                        modifierNbt.putString("Slot", slot.getName());
                    }

                    list.add(modifierNbt);
                }

                nbt.put("AttributeModifiers", list);
            }
        } catch (Exception e) {
            // Ignore any errors here
        }

        return output;
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
