package io.github.theepicblock.polymc.mixins.block.implementations.dontforceintcontrol;

import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PacketByteBuf.class)
public class WriteRegistryValueImplementation {

    @ModifyVariable(method = "writeRegistryValue", at = @At("HEAD"), argsOnly = true)
    private <T> T redirectBlock(T original, IndexedIterable<T> registry) {
        if (registry == Block.STATE_IDS) {
            var player = PacketContext.get().getTarget();
            var polymap = Util.tryGetPolyMap(player);
            return (T)polymap.getClientState((BlockState)original, player);
        }
        return original;
    }
}
