package io.github.theepicblock.polymc.mixins.block.implementations;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.PalettedContainer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PalettedContainer.Data.class)
public class IdListImplementation {
    @Redirect(method = "writePacket", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/chunk/PalettedContainer$Data;storage:Lnet/minecraft/util/collection/PaletteStorage;"))
    private PaletteStorage getData(PalettedContainer.Data container, PacketByteBuf buf)  {
        var originalStorage = container.storage();

        if (!(container.palette() instanceof IdListPalette<?>)) {
            return originalStorage;
        }

        var player = ChunkPacketStaticHack.player.get();
        var polyMap = Util.tryGetPolyMap(player);

        if (!polyMap.isVanillaLikeMap()) {
            return originalStorage;
        }

        // Check if we're actually doing things with blocks
        if (!(container.palette().get(0) instanceof BlockState)) {
            return originalStorage;
        }

        var oldArray = originalStorage.getData();
        var newArray = new long[oldArray.length]; // SAFETY the size of the array mustn't change, otherwise we'd have to inject into getPacketSize as well

        var elementBits = originalStorage.getElementBits(); // The amount of bits per element
        var size = originalStorage.getSize();
        var elementsPerLong = (char)(64 / elementBits);
        var maxValue = (1L << elementBits) - 1L;

        int i = 0; // Counts the elements
        a: for (int j = 0; j < oldArray.length; j++) {
            long oldLong = oldArray[j];
            long newLong = 0;
            for (int k = 0; k < elementsPerLong; k++) {
                var oldElementValue = oldLong & maxValue;
                var newElementValue = transform(oldElementValue, polyMap, player);

                newLong |= newElementValue << (elementBits * k); // Insert the next element
                oldLong >>= elementBits; // Shift oldLong to read the next element

                i++; // Check if we've reached the end already
                if (i >= size) break a;
            }
            newArray[j] = newLong;
        }

        return new PackedIntegerArray(elementBits, size, newArray);
    }

    @Unique
    private long transform(long in, PolyMap map, ServerPlayerEntity playerEntity) {
        var state = Block.getStateFromRawId((int)in);
        return map.getClientStateRawId(state, playerEntity);
    }
}
