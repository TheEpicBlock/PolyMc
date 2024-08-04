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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.item.ItemTransformer;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ModdedResourceContainerImpl;
import io.github.theepicblock.polymc.impl.resource.ResourcePackImplementation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.minecraft.item.ItemStack.ITEM_CODEC;

/**
 * This is the standard implementation of the PolyMap that PolyMc uses by default.
 * You can use a {@link io.github.theepicblock.polymc.api.PolyRegistry} to build one of these more easily.
 */
public class PolyMapImpl implements PolyMap {
    /**
     * The nbt tag name that stores the original item nbt so it can be restored
     * @see PolyMap#getClientItem(ItemStack, ServerPlayerEntity, ItemLocation)
     * @see #recoverOriginalItem(ItemStack)
     */
    private static final String ORIGINAL_ITEM_NBT = "PolyMcOriginal";
    private static final boolean ALWAYS_ADD_CREATIVE_NBT = ConfigManager.getConfig().alwaysSendFullNbt;
    /**
     * Encodes all data that's meant to be server controlled. In practice this is simply all the ItemStack data minus
     * the count
     */
    private static final Codec<ItemStack> ITEM_DATA_CODEC = RecordCodecBuilder.create((instance) -> instance.group(ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry), ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemStack::getComponentChanges)).apply(instance, (id, components) -> new ItemStack(id, 1, components)));
    public static final MapCodec<Optional<ItemStack>> ORIGINAL_ITEM_CODEC = ITEM_DATA_CODEC.optionalFieldOf(ORIGINAL_ITEM_NBT);

    private final ImmutableMap<Item,ItemPoly> itemPolys;
    private final ItemTransformer[] globalItemPolys;
    private final ImmutableMap<Block,BlockPoly> blockPolys;
    private final ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys;
    private final ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys;
    private final ImmutableList<SharedValuesKey.ResourceContainer> sharedValueResources;

    private final boolean hasBlockWizards;

    public PolyMapImpl(ImmutableMap<Item,ItemPoly> itemPolys,
                       ItemTransformer[] globalItemPolys,
                       ImmutableMap<Block,BlockPoly> blockPolys,
                       ImmutableMap<ScreenHandlerType<?>,GuiPoly> guiPolys,
                       ImmutableMap<EntityType<?>,EntityPoly<?>> entityPolys,
                       ImmutableList<SharedValuesKey.ResourceContainer> sharedValueResources) {
        this.itemPolys = itemPolys;
        this.globalItemPolys = globalItemPolys;
        this.blockPolys = blockPolys;
        this.guiPolys = guiPolys;
        this.entityPolys = entityPolys;
        this.sharedValueResources = sharedValueResources;

        this.hasBlockWizards = blockPolys.values().stream().anyMatch(BlockPoly::hasWizard);
    }

    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        if (serverItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack ret = serverItem;

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem, player, location);

        for (ItemTransformer globalPoly : globalItemPolys) {
            ret = globalPoly.transform(serverItem, ret, this, player, location);
        }

        // If max count varies between the client and server item, set the max count.
        if (ret.getMaxCount() != serverItem.getMaxCount()) ret.set(DataComponentTypes.MAX_STACK_SIZE, serverItem.getMaxCount());

        if ((player == null || player.isCreative() || location == ItemLocation.CREATIVE || ALWAYS_ADD_CREATIVE_NBT) && !ItemStack.areItemsAndComponentsEqual(serverItem, ret) && !serverItem.isEmpty()) {

            RegistryOps<NbtElement> registryOps = Util.getRegistryManager(player).getOps(NbtOps.INSTANCE);

            // Preserves the nbt of the original item, so it can be reverted
            var finalRet = ret;
            NbtComponent.DEFAULT.with(registryOps, ORIGINAL_ITEM_CODEC, Optional.of(serverItem)).result().ifPresent((nbt) -> {
                finalRet.set(DataComponentTypes.CUSTOM_DATA, nbt);
            });
        }

        return ret;
    }

    @Override
    public ItemPoly getItemPoly(Item item) {
        return itemPolys.get(item);
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return blockPolys.get(block);
    }

    @Override
    public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
        return guiPolys.get(serverGuiType);
    }

    @Override
    public <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity) {
        return (EntityPoly<T>)entityPolys.get(entity);
    }

    @Override
    public ItemStack reverseClientItem(ItemStack clientItem, @Nullable ServerPlayerEntity player) {
        return recoverOriginalItem(clientItem, player);
    }

    public static ItemStack recoverOriginalItem(ItemStack input, @Nullable ServerPlayerEntity player) {
        var data = input.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) {
            return input;
        }
        var registryOps = Util.getRegistryManager(player).getOps(NbtOps.INSTANCE);
        var result = data.get(registryOps, ORIGINAL_ITEM_CODEC);
        if (result.error().isPresent()) {
            var stack = new ItemStack(Items.CLAY_BALL);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Invalid Item").formatted(Formatting.ITALIC));
            return stack;
        } else {
            // Return the original only if it's present
            var polymcOriginal = result.result().orElseThrow();
            ItemStack recovered_stack = polymcOriginal.orElse(input);
            recovered_stack.setCount(input.getCount());
            return recovered_stack;
        }
    }

    @Override
    public boolean isVanillaLikeMap() {
        return true;
    }

    @Override
    public boolean hasBlockWizards() {
        return hasBlockWizards;
    }

    @Override
    public boolean shouldForceBlockStateSync(BlockState sourceState, BlockState clientState, Direction direction) {
        Block block = clientState.getBlock();
        if (block == Blocks.NOTE_BLOCK) {
            return direction == Direction.UP || direction == Direction.DOWN;
        } else if (block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
            return direction == Direction.DOWN;
        } else if (block == Blocks.TRIPWIRE) {
            if (sourceState == null) return direction.getAxis().isHorizontal();

            //Checks if the connected property for the block isn't what it should be
            //If the source block in that direction is string, it should be true. Otherwise false
            return direction.getAxis().isHorizontal() &&
                    clientState.get(ConnectingBlock.FACING_PROPERTIES.get(direction.getOpposite())) != (sourceState.getBlock() instanceof TripwireBlock);
        }
        return false;
    }

    @Override
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        var moddedResources = new ModdedResourceContainerImpl();
        var pack = new ResourcePackImplementation();

        PolyMc.LOGGER.info("Using: " + moddedResources);

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(moddedResources, pack, logger);
        }

        // Hooks for all itempolys
        this.itemPolys.forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Hooks for all blockpolys
        this.blockPolys.forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Write the resources generated from shared values
        sharedValueResources.forEach((sharedValueResourceContainer) -> {
            try {
                sharedValueResourceContainer.addToResourcePack(moddedResources, pack, logger);
            } catch (Throwable e) {
                logger.warn("Exception whilst generating resources for shared values: " + sharedValueResourceContainer);
                e.printStackTrace();
            }
        });

        // Import the language files for all mods
        var languageKeys = new TreeMap<String, Map<String, String>>(); // The first hashmap is per-language. Then it's translationkey->translation
        for (var lang : moddedResources.locateLanguageFiles()) {
            // Ignore fapi
            if (lang.getLeft().getNamespace().equals("fabric")) continue;
            try (var streamReader = new InputStreamReader(lang.getRight().get(), StandardCharsets.UTF_8)){
                // Copy all the language keys into the main map
                var languageObject = pack.getGson().fromJson(streamReader, JsonObject.class);
                var mainLangMap = languageKeys.computeIfAbsent(lang.getLeft().getPath(), (key) -> new TreeMap<>());
                languageObject.entrySet().forEach(entry -> mainLangMap.put(entry.getKey(), JsonHelper.asString(entry.getValue(), entry.getKey())));
            } catch (Throwable e) {
                logger.error("Couldn't parse lang file " + lang.getLeft());
                e.printStackTrace();
            }
        }
        // It doesn't actually matter which namespace the language files are under. We're just going to put them all under 'polymc-lang'
        languageKeys.forEach((path, translations) -> {
            pack.setAsset("polymc-lang", path, (stream, gson) -> {
                Util.writeJsonToStream(stream, gson, translations);
            });
        });

        // Import sounds
        for (var namespace : moddedResources.getAllNamespaces()) {
            try {
                var soundsRegistry = moddedResources.getSoundRegistry(namespace, "sounds.json");
                if (soundsRegistry == null) continue;
                pack.setSoundRegistry(namespace, "sounds.json", soundsRegistry);
                pack.importRequirements(moddedResources, soundsRegistry, logger);
            } catch (Throwable e) {
                logger.error("Couldn't parse sounds file " + namespace);
                e.printStackTrace();
            }

        }

        try {
            moddedResources.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to close modded resources");
        }
        return pack;
    }

    @Override
    public String dumpDebugInfo() {
        StringBuilder builder = new StringBuilder();

        writeHeader(builder, "ITEMS");
        this.itemPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(item -> item.getKey().getTranslationKey()))
                .forEach(entry -> {
                    var item = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, item, item.getTranslationKey(), poly);
        });

        writeHeader(builder, "BLOCKS");
        this.blockPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(block -> block.getKey().getTranslationKey()))
                .forEach(entry -> {
                    var block = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, block, block.getTranslationKey(), poly);
        });

        writeHeader(builder, "ENTITIES");
        this.entityPolys
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(block -> block.getKey().getTranslationKey()))
                .forEach(entry -> {
                    var entity = entry.getKey();
                    var poly = entry.getValue();
                    addDebugProviderToDump(builder, entity, entity.getTranslationKey(), poly);
                });

        this.sharedValueResources.stream()
                .flatMap(sharedValue -> sharedValue.addDebugSections().stream())
                .forEach(debugSection -> {
                    writeHeader(builder, debugSection.name());
                    debugSection.writer().accept(builder);
                });

        return builder.toString();
    }

    private static void writeHeader(StringBuilder builder, String n) {
        var middleLine = "## " + n + " ##";

        // First line
        middleLine.chars().forEach(i -> builder.append("#"));
        builder.append("\n");
        // Middle line
        builder.append(middleLine);
        builder.append("\n");
        // Last line
        middleLine.chars().forEach(i -> builder.append("#"));
        builder.append("\n");
    }

    private static <T> void addDebugProviderToDump(StringBuilder b, T object, String key, DebugInfoProvider<T> poly) {
        b.append(Util.expandTo(key, 45));
        b.append(" --> ");
        b.append(Util.expandTo(poly.getClass().getName(), 60));
        try {
            String info = poly.getDebugInfo(object);
            if (info != null) {
                b.append("|");
                b.append(info);
            }
        } catch (Exception e) {
            PolyMc.LOGGER.info(String.format("Error whilst getting debug info from '%s' which is registered to '%s'", poly.getClass().getName(), key));
            e.printStackTrace();
        }
        b.append("\n");
    }
}
