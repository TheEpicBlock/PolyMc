package io.github.theepicblock.polymc.mixins.component;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.TransformingPacketCodec;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Function;

@Mixin(ComponentChanges.class)
public class ComponentChangesMixin {
    @Mutable
    @Shadow @Final public static PacketCodec<RegistryByteBuf, ComponentChanges> PACKET_CODEC;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<ComponentChanges> patchCodec(Codec<ComponentChanges> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return transformContent(content);
            }
            return content;
        });
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void patchNetCodec(CallbackInfo ci) {
        PACKET_CODEC = TransformingPacketCodec.encodeOnly(PACKET_CODEC, ((byteBuf, content) -> transformContent(content)));
    }

    @Unique
    private static ComponentChanges transformContent(ComponentChanges content) {
        var player = PacketContext.get();
        var builder = ComponentChanges.builder();
        var map = Util.tryGetPolyMap(player);
        for (var entry : content.entrySet()) {
            if (!map.canReceiveDataComponentType(entry.getKey())) {
                continue;
            } else if (entry.getValue().isPresent() && entry.getValue().get() instanceof TransformingComponent t) {
                //noinspection unchecked
                builder.add((ComponentType<Object>) entry.getKey(), t.polymc$getTransformed(player));
            }

            if (entry.getValue().isPresent()) {
                //noinspection unchecked
                builder.add((ComponentType<Object>) entry.getKey(), entry.getValue().get());
            } else {
                builder.remove(entry.getKey());
            }
        }
        return builder.build();
    }
}
