package io.github.theepicblock.polymc.datagen;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

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

            outBuf.writeVarInt(Block.STATE_IDS.size());
            for (BlockState state : Block.STATE_IDS) {
                writeBlockstate(state, outBuf);
            }

            Files.write(outputFile.toPath(), outBuf.array(), StandardOpenOption.CREATE);

            System.exit(0); // Shut down the server
        } catch (Exception e) {
            // Shutdown the server and tell Gradle it went wrongly
            System.exit(1);
        }
    }

    private static void writeBlockstate(BlockState state, PacketByteBuf buf) {
        Identifier id = Registry.BLOCK.getId(state.getBlock());
        assert id.getNamespace().equals("minecraft"); // This is supposed to be a list with vanilla ids, no modded allowed

        // Write block id
        buf.writeString(id.getPath());

        buf.writeVarInt(state.getEntries().size());
        state.getEntries().forEach((property, value) -> {
            buf.writeString(property.getName());
            buf.writeString(nameValue(property, value));
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T)value);
    }
}
