package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.mixins.block.IdListAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.nio.file.Files;
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
        var blockIdPath = FabricLoader.getInstance().getModContainer(PolyMc.MODID).get().getPath("block-ids");
        var blob = Files.readAllBytes(blockIdPath);

        var buf = new PacketByteBuf(Unpooled.wrappedBuffer(blob));

        int totalBlocks = buf.readVarInt();
        var vanillaBlocks = new ArrayList<BlockState>(totalBlocks);
        for (int i = 0; i < totalBlocks; i++) {
            vanillaBlocks.add(readBlockstate(buf));
        }

        return vanillaBlocks;
    }

    private static BlockState readBlockstate(PacketByteBuf buf) {
        Identifier id = buf.readIdentifier();
        Block block = Registry.BLOCK.get(id);
        var state = block.getDefaultState();

        int totalProperties = buf.readVarInt();
        for (int i = 0; i < totalProperties; i++) {
            var propertyName = buf.readString();
            var valueName = buf.readString();

            var property = block.getStateManager().getProperty(propertyName);
            if (property != null) {
                state = Util.parseAndAddBlockState(state, property, valueName);
            }
        }

        return state;
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
