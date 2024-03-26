package io.github.theepicblock.polymc.datagen;

import io.github.theepicblock.polymc.common.BlockItemType;
import io.github.theepicblock.polymc.common.BlockItemTypeExamples;
import io.netty.buffer.Unpooled;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class PopulateBlockItemInfo {
    public static final Logger LOGGER = LogManager.getLogger("datagen/blockiteminfo");

    public static void doStuff(File outputDir) throws IOException {
        var list = new BlockItemTypeExamples();
        for (var item : Registries.ITEM) {
            if (item.isFood()) continue;

            if (item instanceof BlockItem blockItem) {
                var type = BlockItemType.of(blockItem);
                if (type == null) continue;
                list.add(type, blockItem);
            }
        }

        list.audit(LOGGER);

        PacketByteBuf outBuf = new PacketByteBuf(Unpooled.buffer());
        list.write(outBuf);
        File outputFile = new File(outputDir, "block-item-examples");
        LOGGER.info("block-item-examples: "+outputFile.toPath().toAbsolutePath());

        write(outputFile.toPath(), outBuf, StandardOpenOption.CREATE);
    }

    public static void write(Path path, PacketByteBuf buf, OpenOption... options) throws IOException {
        Objects.requireNonNull(buf);

        try (OutputStream out = Files.newOutputStream(path, options)) {
            int len = buf.readableBytes();
            int rem = len;
            while (rem > 0) {
                int n = Math.min(rem, 8192);
                out.write(buf.array(), (len-rem), n);
                rem -= n;
            }
        }
    }
}
