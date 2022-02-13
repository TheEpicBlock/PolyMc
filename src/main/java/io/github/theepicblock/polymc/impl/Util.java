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
package io.github.theepicblock.polymc.impl;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.mixins.ItemStackAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;

public class Util {
    public static final Gson GSON = new Gson();
    public static final String MC_NAMESPACE = "minecraft";
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    /**
     * Returns true if this identifier is in the minecraft namespace
     */
    public static boolean isVanilla(Identifier id) {
        if (id == null) return false;
        return isNamespaceVanilla(id.getNamespace());
    }

    /**
     * Returns true if this namespace is minecraft
     */
    public static boolean isNamespaceVanilla(String v) {
        return v.equals(MC_NAMESPACE);
    }

    /**
     * Get a BlockState using the properties from a string
     * @param block  base block on which the properties are applied
     * @param string the properties which define this blockstate. Eg: "facing=north,lit=false"
     * @return the blockstate
     */
    public static BlockState getBlockStateFromString(Block block, String string) {
        BlockState v = block.getDefaultState();
        for (String property : string.split(",")) {
            String[] t = property.split("=");
            if (t.length != 2) continue;
            String key = t[0];
            String value = t[1];

            Property<?> prop = block.getStateManager().getProperty(key);
            if (prop != null) {
                v = parseAndAddBlockState(v, prop, value);
            }
        }
        return v;
    }

    public static <T extends Comparable<T>> BlockState parseAndAddBlockState(BlockState v, Property<T> property, String value) {
        Optional<T> optional = property.parse(value);
        if (optional.isPresent()) {
            return v.with(property, optional.get());
        }
        return v;
    }

    /**
     * Splits a string like `facing=east,half=lower,hinge=left,open=false` into ['facing=east', 'half=lower', etc...]
     */
    public static Iterable<String> splitBlockStateString(String string) {
        return COMMA_SPLITTER.split(string);
    }

    /**
     * Get the properties of a blockstate as a string
     * @param state state to extract properties from
     * @return "facing=north,lit=false" for example
     */
    public static String getPropertiesFromBlockState(BlockState state) {
        return getPropertiesFromEntries(state.getEntries());
    }

    public static String getPropertiesFromEntries(Map<Property<?>,Comparable<?>> entries) {
        StringBuilder v = new StringBuilder();
        entries.forEach((property, value) -> {
            v.append(property.getName());
            v.append("=");
            v.append(nameValue(property, value));
            v.append(",");
        });
        String res = v.toString();
        if (res.length() == 0) return res;
        return res.substring(0, res.length() - 1); //this removes the last comma
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T)value);
    }

    /**
     * adds spaces to the end of string s so it has amount length
     */
    public static String expandTo(String s, int amount) {
        int left = amount - s.length();
        if (left >= 0) {
            return s + " ".repeat(left);
        }
        return s;
    }

    public static String expandTo(Object s, int amount) {
        return expandTo(s.toString(), amount);
    }

    public static void copyAll(Path from, Path to) throws IOException {
        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isDirectory()) {
                    Path dest = to.resolve("." + file.toString()); //the dot is needed to make this relative
                    //noinspection ResultOfMethodCallIgnored
                    dest.toFile().mkdirs();
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                return super.visitFile(file, attrs);
            }
        };

        Files.walkFileTree(from, visitor);
    }

    /**
     * Checks if 2 voxelshapes are the same
     */
    public static boolean areEqual(VoxelShape a, VoxelShape b) {
        if (a == b) {
            return true;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return a.getBoundingBox().equals(b.getBoundingBox());
    }

    /**
     * Utility method to get the polyd raw id.
     * PolyMc also redirects {@link Block#getRawIdFromState(BlockState)} but that doesn't respect the player's {@link PolyMap}.
     * This method does.
     * @param state        the BlockState who's raw id is being queried
     * @param playerEntity the player who's {@link PolyMap} we should be using
     * @return the int associated with the state after being transformed by the players {@link PolyMap}
     */
    public static int getPolydRawIdFromState(BlockState state, ServerPlayerEntity playerEntity) {
        PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
        return map.getClientStateRawId(state, playerEntity);
    }

    /**
     * Returns whether the client provided is vanilla-like. As defined in {@link PolyMap#isVanillaLikeMap()}
     * @param client the client who is being checked
     * @return true if the client is vanilla-like, false otherwise
     * @see PolyMap#isVanillaLikeMap()
     */
    public static boolean isPolyMapVanillaLike(ServerPlayerEntity client) {
        return PolyMapProvider.getPolyMap(client).isVanillaLikeMap();
    }

    public static BlockPos fromPalettedContainerIndex(int index) {
        return new BlockPos(index & 0xF, (index >> 8) & 0xF, (index >> 4) & 0xF);
    }

    public static boolean isSectionVisible(ItemStack stack, ItemStack.TooltipSection tooltipSection) {
        int flags = ((ItemStackAccessor)(Object)stack).callGetHideFlags();
        return ItemStackAccessor.callIsSectionVisible(flags, tooltipSection);
    }

    /**
     * @return null if the id can't be parsed or the string is null
     */
    public static Identifier parseId(String id) {
        if (id == null) return null;
        return Identifier.tryParse(id);
    }
}
