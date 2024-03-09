package io.github.theepicblock.polymc.common;

import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class BlockItemTypeExamples {
    /**
     * The maximum amount of examples stored for a single type
     */
    private static final int MAX = 10;
    private final HashMap<BlockItemType, BlockItem[]> inner;

    public BlockItemTypeExamples() {
        inner = new HashMap<>();
    }

    public BlockItemTypeExamples(PacketByteBuf buf) {
        inner = buf.readMap(i -> new HashMap<>(), BlockItemType::new, buf2 -> {
            var length = buf.readVarInt();
            var arr = new BlockItem[length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (BlockItem)Registries.BLOCK.get(buf2.readVarInt()).asItem();
            }
            return arr;
        });
    }

    public void add(@NotNull BlockItemType type, BlockItem item) {
        var array = inner.computeIfAbsent(type, key -> new BlockItem[0]);
        if (array.length >= MAX) {
            return;
        }

        var newArray = ArrayUtils.add(array, item);
        inner.put(type, newArray);
    }

    @Nullable
    public BlockItem[] get(@Nullable BlockItemType type) {
        return inner.get(type);
    }

    public void audit(Logger logger) {
        for (var behaviour : BlockPlacementBehaviour.values()) {
            if (inner.keySet().stream().noneMatch(t -> t.placementBehaviour() == behaviour)) {
                logger.warn("Haven't found any vanilla blocks with behaviour: " + behaviour +
                        "\nThis indicates an error in the behaviour's matching function!!");
            }
        }
    }

    public void write(PacketByteBuf buf) {
        buf.writeMap(inner, BlockItemType::write, BlockItemTypeExamples::writeArr);
    }

    private static void writeArr(PacketByteBuf buf, BlockItem[] arr) {
        buf.writeVarInt(arr.length);
        for (var e : arr) {
            // If these id's changed then this will be the least of our concerns.
            // Besides, the code to fix that exact issue is right next door
            buf.writeVarInt(Registries.BLOCK.getRawId(e.getBlock()));
        }
    }
}
