package io.github.theepicblock.polymc.impl.poly.item;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.common.BlockItemType;
import io.github.theepicblock.polymc.common.BlockItemTypeExamples;
import io.netty.buffer.Unpooled;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class BlockItemInfo {
    public final static SharedValuesKey<BlockItemInfo> KEY = new SharedValuesKey<>(registry -> new BlockItemInfo(), null);

    private BlockItemInfo() {
        try (var exampleFile = PolyMc.class.getResourceAsStream("/block-item-examples")) {
            if (exampleFile != null) {
                var blob = exampleFile.readAllBytes();
                var buf = new PacketByteBuf(Unpooled.wrappedBuffer(blob));
                examples = new BlockItemTypeExamples(buf);
            } else {
                PolyMc.LOGGER.warn("PolyMc was built without including block item info");
            }
        } catch (IOException e) {
            PolyMc.LOGGER.warn("IO Exception whilst trying to read block item info");
            e.printStackTrace();
        }
    }

    /**
     * A couple of examples of vanilla block items that fall into specific categories
     */
    @Nullable
    private BlockItemTypeExamples examples;

    @Nullable
    public BlockItem[] getExamples(BlockItemType type) {
        if (examples == null) {
            return null;
        }
        return examples.get(type);
    }
}
