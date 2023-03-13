package io.github.theepicblock.polymc.datagen;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class Main implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("datagen");
    @Override
    public void onInitialize() {
        try {
            LOGGER.info("Retrieving vanilla ids");
            String output = System.getenv("output-dir");
            File outputDir = new File(output);
            outputDir.mkdirs();

            PacketByteBuf outBuf = new PacketByteBuf(Unpooled.buffer());
            File outputFile = new File(outputDir, "block-ids");
            LOGGER.info("Output: "+outputFile.toPath().toAbsolutePath());

            var properties = new HashSet<Property<?>>();
            for (var block : Registries.BLOCK) {
                properties.addAll(block.getDefaultState().getProperties());
            }

            var propertyTable = new PropertyLookupTable(properties);

            propertyTable.write(outBuf);

            outBuf.writeVarInt(Block.STATE_IDS.size());
            outBuf.writeVarInt(Registries.BLOCK.size());
            for (var block : Registries.BLOCK) {
                writeBlock(block, propertyTable, outBuf);
            }

            Files.write(outputFile.toPath(), outBuf.array(), StandardOpenOption.CREATE);

            System.exit(0); // Shut down the server
        } catch (Exception e) {
            // Shutdown the server and tell Gradle something went wrong
            System.exit(1);
        }
    }

    private static void writeBlock(Block block, PropertyLookupTable table, PacketByteBuf buf) {
        Identifier id = Registries.BLOCK.getId(block);
        assert id.getNamespace().equals("minecraft"); // This is supposed to be a list with vanilla ids, no modded allowed

        // Write block id
        buf.writeString(id.getPath());

        // Write property types
        var properties = block.getStateManager().getProperties();
        buf.writeCollection(properties, (byteBuf, property) -> {
            byteBuf.writeVarInt(table.getPropertyId(property));
        });


        var states = block.getStateManager().getStates();
        // Write first id
        buf.writeVarInt(Block.getRawIdFromState(states.get(0)));
        var lastId = Block.getRawIdFromState(states.get(0))-1;

        buf.writeVarInt(states.size());
        for (var state : states) {
            // Write its property values
            for (var property : properties) {
                buf.writeVarInt(table.getValueId(property, state.get(property)));
            }
            // And finally, write the correct id for it
            if (lastId+1 != Block.getRawIdFromState(state)) {
                throw new AssertionError("Unordered id's!");
            }
            lastId = Block.getRawIdFromState(state);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T)value);
    }
}
