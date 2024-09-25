package io.github.theepicblock.polymc.mixins.component;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ComponentMap.class)
public interface ComponentMapMixin {
    @ModifyVariable(method = "createCodecFromValueMap", at = @At("HEAD"), argsOnly = true)
    private static Codec<Map<ComponentType<?>, Object>> patchCodec(Codec<Map<ComponentType<?>, Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var player = PacketContext.get();
                var polyMap = Util.tryGetPolyMap(player);
                var map = new IdentityHashMap<ComponentType<?>, Object>();
                for (var key : content.keySet()) {
                    var entry = content.get(key);
                    if (entry instanceof TransformingComponent t) {
                        map.put(key, t.polymc$getTransformed(player));
                    } else if (polyMap.canReceiveComponentType(key)) {
                        map.put(key, entry);
                    }
                }

                return map;
            }
            return content;
        });
    }
}
