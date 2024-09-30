package io.github.theepicblock.polymc.mixins.component.transforms;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.minecraft.block.Block;
import net.minecraft.component.type.DebugStickStateComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

@Mixin(DebugStickStateComponent.class)
public class DebugStickStateComponentMixin implements TransformingComponent {
    @Shadow @Final private Map<RegistryEntry<Block>, Property<?>> properties;

    @Override
    public Object polymc$getTransformed(PacketContext context) {
        if (polymc$requireModification(context)) {
            return DebugStickStateComponent.DEFAULT;
        }
        return this;
    }

    @Override
    public boolean polymc$requireModification(PacketContext context) {
        var map = Util.tryGetPolyMap(context);
        for (var key : this.properties.keySet()) {
            if (!map.canReceiveRegistryEntry(Registries.BLOCK, key)) {
                return true;
            }
        }
        return false;
    }
}
