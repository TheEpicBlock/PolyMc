package io.github.theepicblock.polymc.common;

import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record BlockItemType(@NotNull BlockPlacementBehaviour placementBehaviour, SoundEvent placeSound) {
    public BlockItemType(PacketByteBuf buf) {
        this(buf.readEnumConstant(BlockPlacementBehaviour.class), SoundEvent.PACKET_CODEC.decode(buf));
    }

    @Nullable
    public static BlockItemType of(BlockItem blockItem) {
        var block = blockItem.getBlock();
        var behavior = BlockPlacementBehaviour.get(blockItem);
        if (behavior == null) return null;
        var sound = block.getDefaultState().getSoundGroup().getPlaceSound();
        return new BlockItemType(behavior, sound);
    }

    public static void write(PacketByteBuf buf, BlockItemType self) {
        buf.writeEnumConstant(self.placementBehaviour);
        SoundEvent.PACKET_CODEC.encode(buf, self.placeSound);
    }

    // We need custom equals and hashCode because SoundEvent doesn't have a proper one

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockItemType that = (BlockItemType)o;
        return placementBehaviour == that.placementBehaviour &&
                Objects.equals(placeSound.getId(), that.placeSound.getId()) &&
                Objects.equals(placeSound.getDistanceToTravel(1), placeSound.getDistanceToTravel(1)) &&
                Objects.equals(placeSound.getDistanceToTravel(0.5f), placeSound.getDistanceToTravel(0.5f));
    }

    @Override
    public int hashCode() {
        return Objects.hash(placementBehaviour, placeSound.getId(), placeSound.getDistanceToTravel(1), placeSound.getDistanceToTravel(0.5f));
    }
}
