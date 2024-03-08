package io.github.theepicblock.polymc.mixins.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipAppender;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Invoker
    <T extends TooltipAppender> void callAppendTooltip(DataComponentType<T> componentType, Consumer<Text> textConsumer, TooltipContext context);

    @Invoker
    void callAppendAttributeModifiersTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player);
}
