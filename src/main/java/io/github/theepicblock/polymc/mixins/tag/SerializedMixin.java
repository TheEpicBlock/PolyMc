package io.github.theepicblock.polymc.mixins.tag;


import com.google.common.collect.Maps;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import io.github.theepicblock.polymc.impl.mixin.SerializedMixinDuck;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Map;

@Mixin(TagGroup.Serialized.class)
public class SerializedMixin<T> implements SerializedMixinDuck<T> {
    private Map<Identifier,Tag<T>> tags;
    private Registry<T> registry;

    @Override
    public void setTags(Map<Identifier,Tag<T>> v) {
        tags = v;
    }

    @Override
    public void setRegistry(Registry<T> v) {
        registry = v;
    }

    /**
     * This reimplements most of the logic in {@link TagGroup#serialize(Registry)}.
     * This is done here because we've got access to the player here.
     * @author TheEpicBlock
     */
    @Overwrite
    public void writeBuf(PacketByteBuf buf) {
        Map<Identifier,IntList> newMap = Maps.newHashMapWithExpectedSize(tags.size());
        var player = PlayerContextContainer.retrieve(buf);
        var polymap = PolyMapProvider.getPolyMap(player);

        tags.forEach((identifier, tag) -> {
            var values = tag.values();
            var intList = new IntArrayList(values.size());
            for (var value : values) {
                if (polymap.isVanillaLikeMap() && !Util.isVanilla(registry.getId(value))) {
                    // We just ignore modded blocks/items and don't send them as part of the tag.
                    // Sending the polyd version would mess up a bunch of stuff.
                    continue;
                }
                intList.add(registry.getRawId(value));
            }
            newMap.put(identifier, intList);
        });

        buf.writeMap(newMap, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntList);
    }
}
