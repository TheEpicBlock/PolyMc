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
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.mixin.BlockStateDuck;
import io.github.theepicblock.polymc.impl.mixin.TransformingComponent;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;

public class Util {
    public static final Gson GSON = new Gson();
    public static final String MC_NAMESPACE = "minecraft";
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    private static boolean HAS_LOGGED_POLYMAP_ERROR = !ConfigManager.getConfig().logMissingContext;

    public static boolean isVanilla(BlockState state) {
        if (ConfigManager.getConfig().remapVanillaBlockIds) {
            return ((BlockStateDuck)state).polymc$getVanilla();
        }

        return Util.isVanilla(Registries.BLOCK.getId(state.getBlock()));
    }

    /**
     * Returns true if this identifier is in the minecraft namespace
     */
    public static boolean isVanilla(Identifier id) {
        if (id == null) return false;
        return isNamespaceVanilla(id.getNamespace());
    }

    public static boolean isVanillaAndRegistered(RegistryEntry<?> v) {
        return v.getKey().isPresent() && Util.isVanilla(v.getKey().get().getValue());
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
        if (res.isEmpty()) return res;
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

    public static PolyMap tryGetPolyMap(PacketContext context) {
        return tryGetPolyMap(context.getClientConnection());
    }

    public static PolyMap tryGetPolyMap(@Nullable ServerPlayerEntity player) {
        if (player == null) {
            if (!HAS_LOGGED_POLYMAP_ERROR) {
                PolyMc.LOGGER.error("Tried to get polymap but there's no player context. PolyMc will use the default PolyMap. If PolyMc is transforming things it shouldn't, this is why. Further errors of this kind will be silenced. Have a thread dump: ");
                Thread.dumpStack();
                HAS_LOGGED_POLYMAP_ERROR = true;
            }
            return PolyMc.getMainMap();
        }
        if (player instanceof FakePlayer) {
            return NOPPolyMap.INSTANCE;
        }

        return PolyMapProvider.getPolyMap(player);
    }

    @NotNull
    public static PolyMap tryGetPolyMap(@Nullable ServerCommonNetworkHandler handler) {
        return tryGetPolyMap(handler, true);
    }
    public static PolyMap tryGetPolyMap(@Nullable ServerCommonNetworkHandler handler, boolean logWarning) {
        var map = handler == null ? null : PolyMapProvider.getPolyMap(handler);
        if (map == null) {
            if (!HAS_LOGGED_POLYMAP_ERROR && logWarning) {
                PolyMc.LOGGER.error("Tried to get polymap but there's no packet handler context. PolyMc will use the default PolyMap. If PolyMc is transforming things it shouldn't, this is why. Further errors of this kind will be silenced. Have a thread dump: ");
                Thread.dumpStack();
                HAS_LOGGED_POLYMAP_ERROR = true;
            }
            return PolyMc.getMainMap();
        }
        return map;
    }

    @NotNull
    public static PolyMap tryGetPolyMap(@Nullable ClientConnection handler) {
        var map = handler == null ? null : PolyMapProvider.getPolyMap(handler);
        if (map == null) {
            if (!HAS_LOGGED_POLYMAP_ERROR) {
                PolyMc.LOGGER.error("Tried to get polymap but there's no connection context. PolyMc will use the default PolyMap. If PolyMc is transforming things it shouldn't, this is why. Further errors of this kind will be silenced. Have a thread dump: ");
                Thread.dumpStack();
                HAS_LOGGED_POLYMAP_ERROR = true;
            }
            return PolyMc.getMainMap();
        }
        return map;
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
        PolyMap map = Util.tryGetPolyMap(playerEntity);
        return map.getClientStateRawId(state, playerEntity);
    }

    public static int getPolydRawIdFromState(BlockState state, PacketContext context) {
        PolyMap map = Util.tryGetPolyMap(context.getClientConnection());
        return map.getClientStateRawId(state, context.getPlayer());
    }

    /**
     * Returns whether the client provided is vanilla-like. As defined in {@link PolyMap#isVanillaLikeMap()}
     * @param client the client who is being checked
     * @return true if the client is vanilla-like, false otherwise
     * @see PolyMap#isVanillaLikeMap()
     */
    public static boolean isPolyMapVanillaLike(ServerPlayerEntity client) {
        return tryGetPolyMap(client).isVanillaLikeMap();
    }

    public static boolean isPolyMapVanillaLike(ServerCommonNetworkHandler client) {
        return tryGetPolyMap(client).isVanillaLikeMap();
    }

    public static boolean isPolyMapVanillaLike(ClientConnection client) {
        return tryGetPolyMap(client).isVanillaLikeMap();
    }

    public static BlockPos fromPalettedContainerIndex(int index) {
        return new BlockPos(index & 0xF, (index >> 8) & 0xF, (index >> 4) & 0xF);
    }

    /**
     * @return null if the id can't be parsed or the string is null
     */
    public static Identifier parseId(String id) {
        if (id == null) return null;
        return Identifier.tryParse(id);
    }

    public static void writeJsonToStream(OutputStream stream, Gson gson, Object json) throws IOException {
        try (var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        }
    }

    /**
     * Returns a copy of the provided {@link ItemStack}, but with the item set to the target item.
     */
    public static ItemStack copyWithItem(ItemStack original, Item target, @Nullable ServerPlayerEntity player) {
        var out = new ItemStack(target, original.getCount());
        for (var x : original.getComponents().getTypes()) {
            if (original.getComponents().get(x) == null) {
                out.set(x, null);
            }
        }
        var ctx = player == null ? PacketContext.get() : PacketContext.of(player);

        for (ComponentType<?> type : COMPONENTS_TO_COPY) {
            var x = original.get(type);

            if (x instanceof TransformingComponent t) {
                //noinspection unchecked,rawtypes
                out.set((ComponentType)type, t.polymc$getTransformed(ctx));
            } else {
                //noinspection unchecked,rawtypes
                out.set((ComponentType)type, (Object)original.get(type));
            }
        }
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, original.hasGlint());

        return out;
    }

    /**
     * Get the appropriate dynamic registry manager
     */
    @NotNull
    public static RegistryWrapper.WrapperLookup getRegistryManager(PlayerEntity entity) {
        if (entity == null) {
            return getRegistryManager();
        }

        return entity.getRegistryManager();
    }

    /**
     * Get the appropriate dynamic registry manager. Please use
     * {@link #getRegistryManager(PlayerEntity)} unless it's really not possible.
     */
    @NotNull
    public static RegistryWrapper.WrapperLookup getRegistryManager() {
        var ctx = PacketContext.get();
        if (ctx.getRegistryWrapperLookup() != null) {
            return ctx.getRegistryWrapperLookup();
        }

        if (PolyMc.FALLBACK_REGISTRY_MANAGER != null) {
            return PolyMc.FALLBACK_REGISTRY_MANAGER;
        }

        // Fallback to an empty registry
        return DynamicRegistryManager.EMPTY;
    }

    private static final ComponentType<?>[] COMPONENTS_TO_COPY = {DataComponentTypes.CAN_BREAK, DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.BLOCK_ENTITY_DATA, DataComponentTypes.TRIM,
            DataComponentTypes.TOOL,
            DataComponentTypes.ITEM_NAME,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.FOOD,
            DataComponentTypes.FIRE_RESISTANT,
            DataComponentTypes.FIREWORKS,
            DataComponentTypes.FIREWORK_EXPLOSION,
            DataComponentTypes.DAMAGE,
            DataComponentTypes.MAX_DAMAGE,
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.BANNER_PATTERNS,
            DataComponentTypes.BASE_COLOR,
            DataComponentTypes.HIDE_TOOLTIP,
            DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
            DataComponentTypes.CAN_BREAK,
            DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.REPAIR_COST,
            DataComponentTypes.BUNDLE_CONTENTS,
            DataComponentTypes.RARITY,
            DataComponentTypes.LODESTONE_TRACKER,
            DataComponentTypes.ENCHANTMENTS,
            DataComponentTypes.STORED_ENCHANTMENTS,
            DataComponentTypes.POTION_CONTENTS,
            DataComponentTypes.CUSTOM_NAME,
            DataComponentTypes.WRITABLE_BOOK_CONTENT,
            DataComponentTypes.WRITTEN_BOOK_CONTENT,
            DataComponentTypes.DYED_COLOR,
            DataComponentTypes.JUKEBOX_PLAYABLE,
            DataComponentTypes.CONTAINER,
    };

    public static NbtCompound transformBlockEntityNbt(PacketContext context, BlockEntityType<?> type, NbtCompound original) {
        if (original.isEmpty()) {
            return original;
        }
        NbtCompound override = null;

        var lookup = context.getRegistryWrapperLookup() != null ? context.getRegistryWrapperLookup() : null;
        if (lookup == null) {
            return original;
        }
        var polymap = tryGetPolyMap(context.getClientConnection());


        if (original.contains("Items", NbtElement.LIST_TYPE)) {
            var list = original.getList("Items", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                var nbt = list.getCompound(i);
                var stack = ItemStack.fromNbtOrEmpty(lookup, nbt);
                var x = polymap.getClientItem(stack, context.getPlayer(), ItemLocation.EQUIPMENT);
                if (x != stack) {
                    if (override == null) {
                        override = original.copy();
                    }
                    nbt = nbt.copy();
                    nbt.remove("id");
                    nbt.remove("components");
                    nbt.remove("count");
                    override.getList("Items", NbtElement.COMPOUND_TYPE).set(i, x.isEmpty() ? new NbtCompound() : x.encode(lookup, nbt));
                }
            }
        }

        if (original.contains("item", NbtElement.COMPOUND_TYPE)) {
            var stack = ItemStack.fromNbtOrEmpty(lookup, original.getCompound("item"));
            var x = polymap.getClientItem(stack, context.getPlayer(), ItemLocation.EQUIPMENT);
            if (stack != x) {
                if (override == null) {
                    override = original.copy();
                }
                override.put("item", x.encodeAllowEmpty(lookup));
            }
        }

        if (original.contains("components", NbtElement.COMPOUND_TYPE)) {
            var ops = lookup.getOps(NbtOps.INSTANCE);

            var comp = ComponentMap.CODEC.decode(ops, original.getCompound("components"));
            if (comp.isSuccess()) {
                var map = comp.getOrThrow().getFirst();
                ComponentMap.Builder builder = null;

                for (var component : map) {
                    if (component.value() instanceof TransformingComponent transformingComponent && transformingComponent.polymc$requireModification(context)) {
                        if (builder == null) {
                            builder = ComponentMap.builder();
                            builder.addAll(map);
                        }
                        //noinspection unchecked
                        builder.add((ComponentType<? super Object>) component.type(), transformingComponent.polymc$getTransformed(context));
                    } else if (polymap.canReceiveDataComponentType(component.type())) {
                        if (builder == null) {
                            builder = ComponentMap.builder();
                            builder.addAll(map);
                        }
                        builder.add(component.type(), null);
                    }
                }

                if (builder != null) {
                    if (override == null) {
                        override = original.copy();
                    }
                    override.put("components", ComponentMap.CODEC.encodeStart(ops, builder.build()).result().orElse(new NbtCompound()));
                }
            }
        }

        return override != null ? override : original;
    }

    @Nullable
    public static PacketContext getContext(ServerPlayerEntity player) {
        return player == null ? PacketContext.get() : PacketContext.of(player);
    }
}
