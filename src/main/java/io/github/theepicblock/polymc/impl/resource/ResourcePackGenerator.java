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

import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class ResourcePackGenerator {
    // TODO event

    /**
     * Get the path to a file inside PolyMC's resources folder
     * @param path the path to the file inside the resource folder
     */
    public static Path getPolymcPath(String path) {
        return FabricLoader.getInstance().getModContainer("polymc").get().getPath(path);
    }

    public static void cleanAndWrite(PolyMcResourcePack pack, String directory, SimpleLogger logger) {

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path resourcePath = gameDir.resolve(directory).toAbsolutePath();
        resourcePath.toFile().mkdir();

        //Clear up the assets folder
        File assetsFolder = resourcePath.resolve("assets").toFile();
        if (assetsFolder.exists() && assetsFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(assetsFolder);
            } catch (IOException e) {
                logger.warn("Couldn't delete the assets folder. There may still be some unneeded files in there");
            }
        }

        pack.write(resourcePath, logger);
    }

    public static PolyMcResourcePack generate(PolyMap map, SimpleLogger logger) {
        var moddedResources = new ModdedResourceContainerImpl();
        var pack = new ResourcePackImplementation();

        //Let mods register resources via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("polymc", PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerModSpecificResources(moddedResources, pack, logger);
        }

        // Hooks for all itempolys
        map.getItemPolys().forEach((item, itemPoly) -> {
            try {
                itemPoly.addToResourcePack(item, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Hooks for all blockpolys
        map.getBlockPolys().forEach((block, blockPoly) -> {
            try {
                blockPoly.addToResourcePack(block, moddedResources, pack, logger);
            } catch (Exception e) {
                logger.warn("Exception whilst generating resources for " + block.getTranslationKey());
                e.printStackTrace();
            }
        });

        // Add all ArmorMaterial polys
        if (ArmorMaterialPoly.shouldUseFancyPants(map.getArmorMaterialPolys())) {
            try {
                ArmorMaterialPoly.addToResourcePack(map.getArmorMaterialPolys(), moddedResources, pack, logger);
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

        // Import the language files for all mods
        var languageKeys = new HashMap<String, HashMap<String, String>>(); // The first hashmap is per-language. Then it's translationkey->translation
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

    /**
     * @deprecated use {@link #generate(PolyMap, SimpleLogger)} together with {@link #cleanAndWrite(PolyMcResourcePack, String, SimpleLogger)} or {@link PolyMcResourcePack#write(Path, SimpleLogger)}
     */
    @Deprecated
    public static void generate(PolyMap map, String directory, SimpleLogger logger) {
        var pack = generate(map, logger);
        cleanAndWrite(pack, directory, logger);
    }
}
