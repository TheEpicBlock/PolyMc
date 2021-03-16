/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;

import java.util.*;

/**
 * This patch prevents modded blocks/items from appearing in tags when they are synchronised to the client.
 * The client will replace any ids it doesn't recognize with minecraft:air.
 * This can cause issues. For example: a mod places a block in the {@code swimmable} tag.
 * It gets replaced with minecraft:air and the client now thinks it can swim in air.
 * <p>
 * Note: we're not actually mixing into the interface, but we're mixing into the anonymous class in {@link TagGroup#create(Map)}
 * Because TagGroup is an interface, we can't use any injector type mixins on it. So we have to copy over the {@link #serialize} method and change it manually
 */
@SuppressWarnings("MixinInnerClass")
@Mixin(targets = "net/minecraft/tag/TagGroup$1")
public abstract class TagSyncronizePatch<T> implements TagGroup<T> {
    private Map<Identifier,Tag<T>> cache;

    /**
     * Gets all tags except those that are modded
     */
    public Map<Identifier,Tag<T>> getTagsWithoutModded(DefaultedRegistry<T> registry) {
        if (cache != null) return cache;

        Map<Identifier,Tag<T>> original = this.getTags();
        Map<Identifier,Tag<T>> output = new HashMap<>();

        original.forEach((tagIdentifier, tagContents) -> {
            //filter out any tags that have modded identifiers. As they won't be useful to the client anyway.
            if (Util.isVanilla(tagIdentifier)) {
                List<T> newContents = new ArrayList<>();
                tagContents.values().forEach((tag) -> {
                    //filter out any content (blocks, items, etc) that is non vanilla
                    if (Util.isVanilla(registry.getId(tag))) {
                        newContents.add(tag);
                    }
                });
                output.put(tagIdentifier, new ListBackedTag<>(newContents));
            }
        });

        cache = output;
        return output;
    }

    @SuppressWarnings({"unchecked", "WhileLoopReplaceableByForEach"})
    public void toPacket(PacketByteBuf buf, DefaultedRegistry<T> registry) {
        ServerPlayerEntity player = ((PlayerContextContainer)buf).getPolyMcProvidedPlayer(); //See mixins.context.ByteBufPlayerContextContainer
        boolean shouldBePatched = Util.isPolyMapVanillaLike(player);
        Map<Identifier,Tag<T>> map = shouldBePatched ? this.getTagsWithoutModded(registry) : this.getTags();

        //vanilla code:
        buf.writeVarInt(map.size());
        Iterator<?> var4 = map.entrySet().iterator();

        while (var4.hasNext()) {
            Map.Entry<Identifier,Tag<T>> entry = (Map.Entry<Identifier,Tag<T>>)var4.next();
            buf.writeIdentifier(entry.getKey());
            buf.writeVarInt(((Tag<?>)entry.getValue()).values().size());
            Iterator<?> var6 = ((Tag<?>)entry.getValue()).values().iterator();

            while (var6.hasNext()) {
                T object = (T)var6.next();
                buf.writeVarInt(registry.getRawId(object));
            }
        }

    }

    public static class ListBackedTag<T> implements Tag<T> {
        final private List<T> list;

        public ListBackedTag(List<T> list) {
            this.list = list;
        }

        @Override
        public boolean contains(T entry) {
            return list.contains(entry);
        }

        @Override
        public List<T> values() {
            return list;
        }
    }
}
