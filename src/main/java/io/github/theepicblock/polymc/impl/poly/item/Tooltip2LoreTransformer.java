package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.impl.mixin.TransformingDataComponent;
import io.github.theepicblock.polymc.mixins.item.ArmorTrimAccessor;
import io.github.theepicblock.polymc.mixins.item.ItemEnchantmentsComponentAccessor;
import io.github.theepicblock.polymc.mixins.item.ItemStackAccessor;
import it.unimi.dsi.fastutil.objects.AbstractReferenceList;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tooltip2LoreTransformer implements ItemTransformer {
    private static final List<HideableTooltip> HIDEABLE_TOOLTIPS = List.of(
            HideableTooltip.of(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.TRIM, ArmorTrim::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.UNBREAKABLE, UnbreakableComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_BREAK, BlockPredicatesChecker::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker::withShowInTooltip)
    );

    @Override
    public ItemStack transform(ItemStack original, ItemStack input, PolyMap polyMap, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        Item.TooltipContext ctx;
        if (player != null) {
            ctx = Item.TooltipContext.create(player.getWorld());
        } else {
            ctx = Item.TooltipContext.DEFAULT;
        }
        var type = TooltipType.BASIC;

        if (shouldPort(input, original, ctx, type, player)) {
            // Copy if needed
            var output = original == input ? input.copy() : input;
            try {
                var list = original.getTooltip(ctx, player, type);
                if (list.isEmpty()) {
                    input.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                } else {
                    list.removeFirst();
                    var style = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
                    for (int i = 0; i < list.size(); i++) {
                        list.set(i, Text.empty().setStyle(style).append(list.get(i)));
                    }
                    output.set(DataComponentTypes.LORE, new LoreComponent(list, null));
                    output.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    for (var x : HIDEABLE_TOOLTIPS) {
                        x.apply(output);
                    }
                }
            } catch (Throwable e) {
                fallbackPortToLore(output, player, ctx, type);
            }

            return output;
        }
        return input;
    }

    private static boolean shouldPort(ItemStack stack, ItemStack original, Item.TooltipContext ctx, TooltipType type, @Nullable ServerPlayerEntity player) {
        // Checks for components which:
        //  - Add things to the tooltip
        //  - Don't generate said tooltip correctly for modded content
        // Note that these components are a slightly different set from those in `portToLore`. Since that
        // method has to process a continuous block

        // This method checks the following components:
        //   - DataComponentTypes.STORED_ENCHANTMENTS
        //   - DataComponentTypes.ENCHANTMENTS
        //   - DataComponentTypes.ATTRIBUTE_MODIFIERS
        //   - Anything done using Item#appendTooltip

        if (original.contains(DataComponentTypes.HIDE_TOOLTIP)) {
            return false;
        }

        if (TransformingDataComponent.requireTransformForTooltip(original.get(DataComponentTypes.STORED_ENCHANTMENTS), player)) {
            return true;
        }

        if (TransformingDataComponent.requireTransformForTooltip(original.get(DataComponentTypes.ENCHANTMENTS), player)) {
            return true;
        }

        if (TransformingDataComponent.requireTransformForTooltip(original.get(DataComponentTypes.ATTRIBUTE_MODIFIERS), player)
        || (stack.getItem() != original.getItem() && !original.getItem().getAttributeModifiers().modifiers().isEmpty())) {
            return true;
        }

        // Check Item#appendTooltip
        if (!ItemStack.areItemsAndComponentsEqual(original, stack) && !original.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            try {
                original.getItem().appendTooltip(original, ctx, CrashyList.INSTANCE, type);
            } catch (TriedInsertException e) {
                return true;
            }
        }

        return false;
    }

    /**
     * Computes the tooltip serverside and applies it to the target item.
     * Note: modifies the input
     */
    @SuppressWarnings("UnreachableCode")
    public static void fallbackPortToLore(ItemStack input, @Nullable PlayerEntity player, Item.TooltipContext ctx, TooltipType type) {
        // This function will reprocess the following components:
        //   - Anything done using Item#appendTooltip
        //   - DataComponentTypes.TRIM
        //   - DataComponentTypes.STORED_ENCHANTMENTS
        //   - DataComponentTypes.ENCHANTMENTS
        //   - DataComponentTypes.DYED_COLOR
        //   - DataComponentTypes.LORE
        //   - DataComponentTypes.ATTRIBUTE_MODIFIERS

        // To avoid ordering issues, all of these will be processed as one block
        // This block should be continuous, and thus contains some components which wouldn't have needed to
        // be reprocessed if they appeared on their own (such as DYED_COLOR). Again, this is to prevent ordering issues.

        // Otherwise, there might be a situation where, for example, both ENCHANTMENTS and DYED_COLOR are present.
        // If only ENCHANTMENTS was processed, it'd be moved into LORE after DYED_COLOR, instead of before
        // There's no point detecting these situation for any marginal performance boost. The relevant code will only
        // be run iff a situation arises where the ordering would be messed up.

        var invoker = (ItemStackAccessor)(Object)input;
        //noinspection ConstantValue
        assert invoker != null;

        var lore = new ArrayList<Text>();
        Consumer<Text> append_function = (text) -> {
            if (text.getStyle().isEmpty()) {
                // Make sure the lore doesn't mess up the styling
                // It doesn't matter which style we set, as long as the style no longer counts as empty
                lore.add(text.copy().setStyle(Style.EMPTY.withItalic(text.getStyle().isItalic())));
            } else {
                lore.add(text);
            }
        };

        /////////////////
        // Precompute the lore
        // Should match the order of ItemStack#getTooltip

        var hasAdditional = addAdditionalTooltip(input, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.TRIM, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.STORED_ENCHANTMENTS, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.ENCHANTMENTS, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.DYED_COLOR, ctx, append_function, type);
        invoker.callAppendTooltip(DataComponentTypes.LORE, ctx, append_function, type);
        invoker.callAppendAttributeModifiersTooltip(append_function, player);

        /////////////////
        // Ensure that the components are showInTooltip = false

        if (hasAdditional) {
            input.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        }

        var trim = input.get(DataComponentTypes.TRIM);
        if (trim != null && ((ArmorTrimAccessor)trim).isShowInTooltip()) {
            input.set(DataComponentTypes.TRIM, new ArmorTrim(trim.getMaterial(), trim.getPattern(), false));
        }

        var stored_enchants = (ItemEnchantmentsComponentAccessor)input.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored_enchants != null && stored_enchants.isShowInTooltip()) {
            input.set(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponentAccessor.createItemEnchantmentsComponent(stored_enchants.getEnchantments(), false));
        }

        var enchants = (ItemEnchantmentsComponentAccessor)input.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null && enchants.isShowInTooltip()) {
            input.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponentAccessor.createItemEnchantmentsComponent(enchants.getEnchantments(), false));
        }

        var dyed = input.get(DataComponentTypes.DYED_COLOR);
        if (dyed != null && dyed.showInTooltip()) {
            input.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(dyed.rgb(), false));
        }

        var attributeModifiers = input.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null && attributeModifiers.showInTooltip()) {
            input.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(attributeModifiers.modifiers(), false));
        }

        /////////////////
        // Insert the LORE component
        // No need to set styledLines, it's not used for serialization
        input.set(DataComponentTypes.LORE, new LoreComponent(lore, null));
    }

    /**
     * Adds tooltip lines appended by {@link net.minecraft.item.Item#appendTooltip(ItemStack, Item.TooltipContext, List, TooltipType)}
     *
     * @return true if something was inserted
     */
    private static boolean addAdditionalTooltip(ItemStack stack, Item.TooltipContext ctx, Consumer<Text> textConsumer, TooltipType type) {
        var list = new ArrayList<Text>();
        stack.getItem().appendTooltip(stack, ctx, list, type);
        list.forEach(textConsumer);
        return !list.isEmpty();
    }

    private static class CrashyList<T> extends AbstractReferenceList<T> {
        public static final CrashyList<Text> INSTANCE = new CrashyList<>();

        @Override
        public int size() {
            return 0;
        }

        @Override
        public T get(int index) {
            return null;
        }

        @Override
        public void add(int index, T t) {
            throw new TriedInsertException();
        }
    }

    private static class TriedInsertException extends RuntimeException {

    }

    private record HideableTooltip<T>(ComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {

        public static <T> HideableTooltip<T> of(ComponentType<T> type, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, x -> true, setter);
        }

        public static <T> HideableTooltip<T> of(ComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldSet, setter);
        }

        public static <T> HideableTooltip<T> ofNeg(ComponentType<T> type, Predicate<T> shouldntSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldntSet.negate(), setter);
        }

        public void apply(ItemStack output) {
            var data = output.get(type);
            if (data != null && this.shouldSet.test(data)) {
                output.set(type, setter.setTooltip(data, false));
            }
        }

        interface TooltipSetter<T> {
            T setTooltip(T val, boolean value);
        }
    }
}
