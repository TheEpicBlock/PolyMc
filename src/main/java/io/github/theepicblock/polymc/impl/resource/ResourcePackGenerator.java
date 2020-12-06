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

import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.resource.JsonSoundsRegistry;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ResourcePackGenerator {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generate() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path resourcePath = gameDir.resolve("resource").toAbsolutePath();
        resourcePath.toFile().mkdir();

        ResourcePackMaker pack;
        if (ConfigManager.getConfig().resourcepack.advancedDiscovery) {
            File tempDir = gameDir.resolve("resource_temp").toFile();
            tempDir.mkdirs();
            Path tempPath = tempDir.toPath().toAbsolutePath();
            pack = new AdvancedResourcePackMaker(resourcePath, tempPath);
        } else {
            pack = new ResourcePackMaker(resourcePath);

            if (FabricLoader.getInstance().getModContainer("artifice").isPresent()) {
                PolyMc.LOGGER.error("Artifice was detected, but the default PolyMc resource pack maker is not compatible with Artifice");
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

        //Copy over sound files
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            if (pack.checkAsset(modId,"sounds.json")) {
                pack.copyAsset(modId,"sounds.json"); //copy over the sounds.json file to the pack

                //read the sounds.json file to parse the needed sound files.
                InputStreamReader reader = pack.getAsset(modId,"sounds.json");
                JsonReader jReader = new JsonReader(reader);
                Map<String,JsonSoundsRegistry.SoundEntry> sounds = pack.getGson().fromJson(jReader, JsonSoundsRegistry.TYPE);

                //copy the individual ogg files specified in the sounds.json
                sounds.values().forEach(soundEntry -> {
                    for (String soundString : soundEntry.sounds) {
                        Identifier soundId = Identifier.tryParse(soundString);
                        if (soundId == null) {
                            PolyMc.LOGGER.warn(String.format("Invalid sound id %s in %s provided by %s", soundString, soundEntry.category, modId));
                            continue;
                        }

                        pack.copySound(soundId.getNamespace(), soundId.getPath());
                    }
                });
            }
        }

        pack.saveAll();
    }
}
