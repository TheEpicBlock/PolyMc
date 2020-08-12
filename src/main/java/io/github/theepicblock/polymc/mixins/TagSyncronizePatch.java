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

import io.github.theepicblock.polymc.Util;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This patch prevents modded blocks/items from appearing in tags when they are synchronised to the client.
 * The client will replace any ids it doesn't recognize with minecraft:air.
 * This can cause issues. For example: a mod places a block in the swimmable tag.
 * It get's replaced with air and the client now thinks it can swim in air.
 */
@SuppressWarnings("MixinInnerClass")
@Mixin(TagGroup.class)
public abstract class TagSyncronizePatch<T> {
    private Map<Identifier, Tag<T>> cache;

    /**
     * Redirects the call to get the tags in the to packet function so we can filter out all of the modded tags.
     */
    @Redirect(method = "toPacket(Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/util/registry/DefaultedRegistry;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tag/TagGroup;getTags()Ljava/util/Map;"))
    public Map<Identifier, Tag<T>> getTagsRedirect(TagGroup<T> registryTagContainer, DefaultedRegistry<T> registry) {
        if (cache != null) {
            return cache;
        }

        Map<Identifier, Tag<T>> original = registryTagContainer.getTags();
        Map<Identifier, Tag<T>> output = new HashMap<>();

        for (Map.Entry<Identifier, Tag<T>> originalEntry : original.entrySet()) {
            if (Util.isVanilla(originalEntry.getKey())) {
                //This tag isn't modded, we now need to figure out if it has any modded values in it
                Tag<T> originalTag = originalEntry.getValue();
                List<T> newList = new ArrayList<>();
                originalTag.values().forEach((tag) -> {
                    //loop thru all the tags and only add it to the new list if it's vanilla
                    if (Util.isVanilla(registry.getId(tag))) {
                        newList.add(tag);
                    }
                });
                Tag<T> newTag = new DumbListTag<>(newList);
                output.put(originalEntry.getKey(),newTag);
            }
        }
        cache = output;

        return output;
    }

    public static class DumbListTag<T> implements Tag<T> {
        final private List<T> list;

        public DumbListTag(List<T> list) {
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
