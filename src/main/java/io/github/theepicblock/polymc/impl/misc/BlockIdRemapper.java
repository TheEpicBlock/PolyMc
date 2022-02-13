package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.mixins.block.IdListAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockIdRemapper {
    public static void remapFromInternalList() {
        try {
            var list = readInternalList();
            remapBlocks(list);
            PolyMc.LOGGER.info("Successfully remapped "+list.size()+" vanilla blocks");
        } catch (Exception e) {
            PolyMc.LOGGER.error("Couldn't remap block ids");
            e.printStackTrace();
        }
    }

    private static List<BlockState> readInternalList() throws IOException {
        var blob = PolyMc.class.getResourceAsStream("/block-ids").readAllBytes();

        var buf = new PacketByteBuf(Unpooled.wrappedBuffer(blob));

        var propertyLookupTable = new PropertyLookupTable(buf);

        var vanillaBlocks = new BlockState[buf.readVarInt()];
        int totalBlocks = buf.readVarInt();
        for (int i = 0; i < totalBlocks; i++) {
            readBlock(buf, propertyLookupTable, vanillaBlocks);
        }

        return List.of(vanillaBlocks);
    }

    private static void readBlock(PacketByteBuf buf, PropertyLookupTable table, BlockState[] outputList) {
        var path = buf.readString();
        var id = new Identifier(path);
        Block block = Registry.BLOCK.get(id);

        var baseState = block.getDefaultState();

        var properties = buf.readCollection(ArrayList::new,
                (buf0) -> table.getProperty(buf.readVarInt(), block));

        var firstStateId = buf.readVarInt();

        var amountOfStates = buf.readVarInt();
        for (int i = 0; i < amountOfStates; i++) {
            var state = baseState;
            for (var property : properties) {
                var valueId = buf.readVarInt();
                state = blockStateWith(state, property, table.getValue(property, valueId));
            }
            var stateId = firstStateId+i;
            if (outputList[stateId] != null) {
                throw new IllegalStateException("Duplicate blockstate for id "+stateId+" : "+path);
            }
            outputList[stateId] = state;
        }
    }

    private static <T extends Comparable<T>, V extends T> BlockState blockStateWith(BlockState state, Property<T> property, Object value) {
        return state.with(property, (V)value);
    }

    private static void remapBlocks(List<BlockState> vanillaBlocks) {
        var accessor = (IdListAccessor<BlockState>)Block.STATE_IDS;

        var blockList = accessor.getList();
        var idMap = accessor.getIdMap();

        var blockListCopy = List.copyOf(blockList);

        blockList.clear();
        blockList.addAll(vanillaBlocks);

        for (BlockState state : blockListCopy) {
            if (!vanillaBlocks.contains(state)) {
                blockList.add(state);
            }
        }

        // Update idMap to match new ids
        idMap.clear();
        for (int i = 0; i < blockList.size(); i++) {
            var state = blockList.get(i);
            idMap.put(state, i);
        }
    }
}
