package io.github.theepicblock.polymc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;
import java.util.function.Function;

@Mixin(HoverEvent.class)
public abstract class HoverEventFixerMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<HoverEvent> replaceCodec(Codec<HoverEvent> codec) {
        return codec.xmap(Function.identity(), content -> {
            var ctx = PacketContext.get();

            if (ctx.getClientConnection() != null) {
                var map = Util.tryGetPolyMap(ctx.getClientConnection());
                if (content.getAction() == HoverEvent.Action.SHOW_ITEM) {
                    var stack = Objects.requireNonNull(content.getValue(HoverEvent.Action.SHOW_ITEM)).asStack();
                    return new HoverEvent(HoverEvent.Action.SHOW_ITEM,
                            new HoverEvent.ItemStackContent(map.getClientItem(stack, ctx.getPlayer(), ItemLocation.TEXT)));
                } else if (content.getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    var val = Objects.requireNonNull(content.getValue(HoverEvent.Action.SHOW_ENTITY));
                    if (map.getEntityPoly(val.entityType) != null) {
                        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texts.join(val.asTooltip(), Text.literal("\n")));
                    }
                }
            }
            return content;
        });
    }
}
