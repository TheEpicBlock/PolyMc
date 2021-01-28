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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;

public class Util {
    public static final String MC_NAMESPACE = "minecraft";
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

    private static <T extends Comparable<T>> BlockState parseAndAddBlockState(BlockState v, Property<T> property, String value) {
        Optional<T> optional = property.parse(value);
        if (optional.isPresent()) {
            return v.with(property, optional.get());
        }
        return v;
    }

    /**
     * Get the properties of a blockstate as a string
     * @param state state to extract properties from
     * @return "facing=north,lit=false" for example
     */
    public static String getPropertiesFromBlockState(BlockState state) {
        return getPropertiesFromEntries(state.getEntries());
    }

    public static String getPropertiesFromEntries(Map<Property<?>, Comparable<?>> entries) {
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
            StringBuilder out = new StringBuilder().append(s);
            for (int i = 0; i < left; i++) {
                out.append(" ");
            }
            return out.toString();
        }
        return s;
    }

    public static String expandTo(Object s, int amount) {
        return expandTo(s.toString(), amount);
    }

    public static void copyAll(Path from, Path to) throws IOException {
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
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

        Files.walkFileTree(from,visitor);
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
     * moves all modded enchantments into the lore tag
     * @param item item whose enchantments to move
     * @return the converted item
     */
    public static ItemStack portEnchantmentsToLore(ItemStack item) {
        //noinspection ConstantConditions
        if (item.hasTag() && item.getTag().contains("Enchantments", 9)) {
            //check if the enchantments aren't hidden
            int hideFlags = item.getTag().contains("HideFlags", 99) ? item.getTag().getInt("HideFlags") : 0;
            if ((hideFlags & ItemStack.TooltipSection.ENCHANTMENTS.getFlag()) == 0) {
                ItemStack stack = item.copy();
                ListTag enchantments = stack.getEnchantments();

                //iterate through the enchantments
                for (Tag tag : enchantments) {
                    if (tag.getType() != 10) continue; //this is not a compound tag
                    CompoundTag compoundTag = (CompoundTag)tag;

                    Identifier id = Identifier.tryParse(compoundTag.getString("id"));

                    if (!Util.isVanilla(id) && id != null) {
                        Registry.ENCHANTMENT.getOrEmpty(id).ifPresent((enchantment) -> {
                            //iterator.remove();
                            Text name = enchantment.getName(compoundTag.getInt("lvl"));

                            CompoundTag displayTag = stack.getOrCreateSubTag("display");
                            if (!displayTag.contains("Lore")) {
                                displayTag.put("Lore", new ListTag());
                            }
                            displayTag.getList("Lore", 8).add(StringTag.of(Text.Serializer.toJson(name)));
                        });
                    }
                }
                return stack;
            }
        }
        return item;
    }
}
