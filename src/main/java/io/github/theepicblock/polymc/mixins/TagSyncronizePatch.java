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
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MixinInnerClass")
@Mixin(RegistryTagContainer.class)
public class TagSyncronizePatch<T> {
    @Shadow @Final private Registry<T> registry;
    private Map<Identifier, Tag<T>> entriesWithoutModdedCache;

//    @Redirect(method = "toPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
//    public <T> int getRawIdRedirect(Registry<T> registry, T entry) {
//        if (!Util.isVanilla(registry.getId(entry))) {
//            return -1;
//        }
//        return registry.getRawId(entry);
//    }
//
//    @Redirect(method = "toPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;"))
//    public PacketByteBuf writeVarIntRedirect(PacketByteBuf packetByteBuf, int i) {
//        if (i == -1) {
//            return packetByteBuf;
//        }
//        return packetByteBuf.writeVarInt(i);
//    }

    @Redirect(method = "toPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tag/RegistryTagContainer;getEntries()Ljava/util/Map;"))
    public Map<Identifier, Tag<T>> getEntriesRedirect(RegistryTagContainer<T> registryTagContainer) {
        if (entriesWithoutModdedCache != null) {
            return entriesWithoutModdedCache;
        }

        Map<Identifier, Tag<T>> original = registryTagContainer.getEntries();
        Map<Identifier, Tag<T>> ret = new HashMap<>();

        for (Map.Entry<Identifier, Tag<T>> e : original.entrySet()) {
            if (Util.isVanilla(e.getKey())) {
                Tag<T> originalTag = e.getValue();
                List<T> newList = new ArrayList<>();
                originalTag.values().forEach((tag) -> {
                    if (Util.isVanilla(registry.getId(tag))) {
                        newList.add(tag);
                    }
                });
                Tag<T> newTag = new DumbListTag<>(newList);
                ret.put(e.getKey(),newTag);
            }
        }
        entriesWithoutModdedCache = ret;

        return ret;
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
