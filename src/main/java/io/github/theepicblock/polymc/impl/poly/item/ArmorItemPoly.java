package io.github.theepicblock.polymc.impl.poly.item;

import com.google.common.collect.Multimap;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class ArmorItemPoly extends DamageableItemPoly {

    private ArmorMaterial material;
    private EquipmentSlot slot;
    private PolyRegistry registry;
    private Item replacementItem;

    /**
     * Get the correct replacement item for the given slot
     * @param slot
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

    public ArmorItemPoly(PolyRegistry builder, ArmorItem base) {
        this(builder, base, getReplacementItem(base.getSlotType()));
    }

    public ArmorItemPoly(PolyRegistry builder, ArmorItem base, Item replacementItem) {
        super(builder.getCMDManager(), base, replacementItem);

        this.material = base.getMaterial();
        this.slot = base.getSlotType();
        this.replacementItem = replacementItem;

        // Also make sure the ArmorMaterial poly has been registered!
        if (!builder.hasArmorMaterialPoly(material)) {
            builder.registerArmorMaterialPoly(material, this);
        }

        this.registry = builder;
    }

    public Item getReplacementItem() {
        return this.replacementItem;
    }

    public boolean useColorId() {
        return true;
    }

    @Override
    public ItemStack getClientItem(ItemStack input) {
        ItemStack output = super.getClientItem(input);

        // See if the this poly armor uses a color id (and thus FancyPants)
        if (this.useColorId()) {
            ArmorMaterialPoly materialPoly = registry.getArmorMaterialPoly(material);

            if (materialPoly != null && materialPoly.getColorId() != null) {
                NbtCompound nbt = output.getOrCreateSubNbt("display");
                nbt.putInt("color", materialPoly.getColorId());
            }
        }

        // Get the armor attribute modifiers, so the correct stats are shown on the tooltip
        try {
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = input.getItem().getAttributeModifiers(slot);

            if (!multimap.isEmpty()) {
                NbtCompound nbt = output.getOrCreateNbt();
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
}
