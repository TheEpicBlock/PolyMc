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
package io.github.theepicblock.polymc.impl.resource;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ResourcePackGenerator {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generate() {
        File gameDir = FabricLoader.getInstance().getGameDirectory();
        File resourceDir = new File(gameDir, "resource");
        resourceDir.mkdirs();
        Path path = resourceDir.toPath().toAbsolutePath();

        ResourcePackMaker pack;
        if (ConfigManager.getConfig().resourcepack.advancedDiscovery) {
            File tempDir = new File(gameDir, "resource_temp");
            tempDir.mkdirs();
            Path tempPath = tempDir.toPath().toAbsolutePath();
            pack = new AdvancedResourcePackMaker(path, tempPath);
        } else {
            pack = new ResourcePackMaker(path);

            if (FabricLoader.getInstance().getModContainer("artifice").isPresent()) {
                PolyMc.LOGGER.error("Artifice was detected, but the default PolyMc resourcepack maker is not compatible with Artice");
                PolyMc.LOGGER.error("Please switch to the advanced generator in the config.");
            }
        }

        //Clear up the assets folder
        File assetsFolder = pack.getBuildLocation().resolve("assets").toFile();
        if (assetsFolder.exists() && assetsFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(assetsFolder);
            } catch (IOException e) {
                PolyMc.LOGGER.warn("Couldn't delete the assets folder. There may still be some unneeded files in there");
            }
        }

        //Put the pack.mcmeta in there if it doesn't exist yet
        if (!pack.getBuildLocation().resolve("pack.mcmeta").toFile().exists()) {
            pack.copyFileDirect("polymc", "pack.mcmeta");
        }

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(pack);
        }

        //Hooks for all itempolys
        PolyMc.getMap().getItemPolys().forEach((item, itemPoly) -> {
            try {
                itemPoly.AddToResourcePack(item, pack);
            } catch (Exception e) {
                PolyMc.LOGGER.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        //Hooks for all blockpolys
        PolyMc.getMap().getBlockPolys().forEach((block, blockPoly) -> {
            try {
                blockPoly.AddToResourcePack(block, pack);
            } catch (Exception e) {
                PolyMc.LOGGER.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        //Get all lang files from all mods
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            Path langPath = mod.getPath(String.format("assets/%s/lang", modId));
            if (!Files.exists(langPath)) continue;
            try {
                Stream<Path> pathStream = Files.list(langPath);
                pathStream.forEach((langFile) -> {
                    pack.copyAsset(modId, "lang/" + langPath.relativize(langFile));
                });
            } catch (IOException e) {
                PolyMc.LOGGER.warn("Exception whilst copying lang files from " + modId);
                e.printStackTrace();
            }
        }

        pack.saveAll();
    }
}
