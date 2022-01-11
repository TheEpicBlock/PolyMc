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

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.resource.JsonSoundsRegistry;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class ResourcePackGenerator {

    /**
     * Get the path to a file inside PolyMC's resources folder
     * @param path the path to the file inside the resource folder
     */
    public static Path getPolymcPath(String path) {
        return FabricLoader.getInstance().getModContainer("polymc").get().getPath(path);
    }

    /**
     * Generates a resource pack
     * @param map       {@link PolyMap} to generate the resource from
     * @param directory directory to output files in. Relative to the game directory
     * @param logger    output of the log messages
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generate(PolyMap map, String directory, SimpleLogger logger) {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path resourcePath = gameDir.resolve(directory).toAbsolutePath();
        resourcePath.toFile().mkdir();

        ResourcePackMaker pack;
        if (ConfigManager.getConfig().resourcepack.advancedDiscovery) {
            File tempDir = gameDir.resolve("resource_temp").toFile();
            tempDir.mkdirs();
            Path tempPath = tempDir.toPath().toAbsolutePath();
            pack = new AdvancedResourcePackMaker(resourcePath, tempPath, logger);
        } else {
            pack = new ResourcePackMaker(resourcePath, logger);
        }

        //Clear up the assets folder
        File assetsFolder = pack.getBuildLocation().resolve("assets").toFile();
        if (assetsFolder.exists() && assetsFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(assetsFolder);
            } catch (IOException e) {
                logger.warn("Couldn't delete the assets folder. There may still be some unneeded files in there");
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
        map.getItemPolys().forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, pack);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        //Hooks for all blockpolys
        map.getBlockPolys().forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, pack);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Add all ArmorMaterial polys
        if (ArmorMaterialPoly.shouldUseFancyPants(map.getArmorMaterialPolys())) {
            try {
                ArmorMaterialPoly.addToResourcePack(map.getArmorMaterialPolys(), pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for ArmorMaterialPolys");
                e.printStackTrace();
            }
        } else {
            map.getArmorMaterialPolys().forEach((armorMaterial, armorMaterialPoly) -> {
                try {
                    armorMaterialPoly.addToResourcePack(pack);
                } catch (Exception e) {
                    logger.warn("Exception whilst generating armor material resources for " + armorMaterial.getName());
                    e.printStackTrace();
                }
            });
        }

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
            } catch (Exception e) {
                logger.warn("Exception whilst copying lang files from " + modId);
                e.printStackTrace();
            }
        }

        //Copy over sound files
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            if (pack.checkAsset(modId, "sounds.json")) {
                try {
                    pack.copyAsset(modId, "sounds.json"); //copy over the sounds.json file to the pack

                    //read the sounds.json file to parse the needed sound files.
                    InputStreamReader reader = pack.getAsset(modId, "sounds.json");
                    JsonReader jReader = new JsonReader(reader);
                    Map<String,JsonSoundsRegistry.SoundEventEntry> sounds = pack.getGson().fromJson(jReader, JsonSoundsRegistry.TYPE);

                    //copy the individual ogg files specified in the sounds.json
                    sounds.values().forEach(soundEventEntry -> {
                        for (JsonElement soundEntry : soundEventEntry.sounds) {
                            String namespaceString = JsonSoundsRegistry.getNamespace(soundEntry);
                            Identifier namespace = Identifier.tryParse(namespaceString);
                            if (namespace == null) {
                                logger.warn(String.format("Invalid sound id %s in %s provided by %s", namespaceString, soundEventEntry.category, modId));
                                continue;
                            }

                            pack.copySound(namespace.getNamespace(), namespace.getPath());
                        }
                    });
                } catch (Exception e) {
                    logger.error("Failed to copy sounds.json for mod: " + modId);
                    e.printStackTrace();
                }
            }
        }

        pack.saveAll();
    }
}
