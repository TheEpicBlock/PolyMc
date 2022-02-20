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
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

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
        ItemStack ret = serverItem;
        NbtCompound originalNbt = serverItem.writeNbt(new NbtCompound());

        ItemPoly poly = itemPolys.get(serverItem.getItem());
        if (poly != null) ret = poly.getClientItem(serverItem, location);

        for (ItemTransformer globalPoly : globalItemPolys) {
            ret = globalPoly.transform(ret, player, location);
        }

        if ((player == null || player.isCreative()) && !ItemStack.canCombine(serverItem, ret) && !serverItem.isEmpty()) {
            // Preserves the nbt of the original item so it can be reverted
            ret = ret.copy();
            ret.setSubNbt(ORIGINAL_ITEM_NBT, originalNbt);
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
    public ItemStack reverseClientItem(ItemStack clientItem) {
        return recoverOriginalItem(clientItem);
    }

    public static ItemStack recoverOriginalItem(ItemStack input) {
        if (input.getNbt() == null || !input.getNbt().contains(ORIGINAL_ITEM_NBT, NbtType.COMPOUND)) {
            return input;
        }

        NbtCompound tag = input.getNbt().getCompound(ORIGINAL_ITEM_NBT);
        ItemStack stack = ItemStack.fromNbt(tag);
        stack.setCount(input.getCount()); // The clientside count is leading, to support middle mouse button duplication and stack splitting and such

        if (stack.isEmpty() && !input.isEmpty()) {
            stack = new ItemStack(Items.CLAY_BALL);
            stack.setCustomName(new LiteralText("Invalid Item").formatted(Formatting.ITALIC));
        }
        return stack;
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
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        var moddedResources = new ModdedResourceContainerImpl();
        var pack = new ResourcePackImplementation();

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(moddedResources, pack, logger);
        }

        // Hooks for all itempolys
        this.itemPolys.forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Hooks for all blockpolys
        this.blockPolys.forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Write the resources generated from shared values
        sharedValueResources.forEach((sharedValueResourceContainer) -> {
            try {
                sharedValueResourceContainer.addToResourcePack(moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for shared values: " + sharedValueResourceContainer);
                e.printStackTrace();
            }
        });

        // Import the language files for all mods
        var languageKeys = new HashMap<String,HashMap<String, String>>(); // The first hashmap is per-language. Then it's translationkey->translation
        for (var lang : moddedResources.locateLanguageFiles()) {
            // Ignore fapi
            if (lang.getNamespace().equals("fabric")) continue;
            for (var stream : moddedResources.getInputStreams(lang.getNamespace(), lang.getPath())) {
                // Copy all of the language keys into the main map
                var languageObject = pack.getGson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                var mainLangMap = languageKeys.computeIfAbsent(lang.getPath(), (key) -> new HashMap<>());
                languageObject.entrySet().forEach(entry -> mainLangMap.put(entry.getKey(), JsonHelper.asString(entry.getValue(), entry.getKey())));
            }
        }
        // It doesn't actually matter which namespace the language files are under. We're just going to put them all under 'polymc-lang'
        languageKeys.forEach((path, translations) -> {
            pack.setAsset("polymc-lang", path, (location, gson) -> {
                try (var writer = new FileWriter(location.toFile())) {
                    gson.toJson(translations, writer);
                }
            });
        });

        // Import sounds
        for (var namespace : moddedResources.getAllNamespaces()) {
            var soundsRegistry = moddedResources.getSoundRegistry(namespace, "sounds.json");
            if (soundsRegistry == null) continue;
            pack.setSoundRegistry(namespace, "sounds.json", soundsRegistry);
            pack.importRequirements(moddedResources, soundsRegistry, logger);
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
        builder.append("###########\n## ITEMS ##\n###########\n");
        this.itemPolys.forEach((item, poly) -> {
            addDebugProviderToDump(builder, item, item.getTranslationKey(), poly);
        });
        builder.append("############\n## BLOCKS ##\n############\n");
        this.blockPolys.forEach((block, poly) -> {
            addDebugProviderToDump(builder, block, block.getTranslationKey(), poly);
        });
        return builder.toString();
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
