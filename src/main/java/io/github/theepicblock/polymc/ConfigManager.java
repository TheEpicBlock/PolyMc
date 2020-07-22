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
package io.github.theepicblock.polymc;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ConfigManager {
    private static Config config;

    /**
     * loads the config
     */
    public static void generateConfig() {
        File configDir = FabricLoader.getInstance().getConfigDirectory();
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdirs();
        File configFile = new File(configDir,"polymc.json");

        if (!configFile.exists()) {
            Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("polymc");
            if (container.isPresent()) {
                ModContainer polymcContainer = container.get();
                Path defaultConfig = polymcContainer.getPath("defaultconfig.json");

                try {
                    Files.copy(defaultConfig, Paths.get(configFile.getAbsolutePath()));
                } catch (IOException e) {
                    PolyMc.LOGGER.warn("error whilst copying over default config. An error trying to load said config will most likely appear soon");
                    e.printStackTrace();
                }
            } else {
                PolyMc.LOGGER.warn("Couldn't copy over default config file. An error trying to load said config will most likely appear soon");
                PolyMc.LOGGER.warn("The modcontainer for 'polymc' couldn't be found.");
                PolyMc.LOGGER.warn("Did someone change the modid in the fabric.mod.json!?");
            }
        }
        Gson gson = new Gson();

        try {
            JsonReader reader = new JsonReader(new FileReader(configFile));
            ConfigManager.config = gson.fromJson(reader, Config.class);
        } catch (FileNotFoundException e) {
            PolyMc.LOGGER.warn("Couldn't find config file: " + configFile.getPath());
        }
    }

    /**
     * Gets the polymap needed to translate from server items to client items.
     * @return the PolyMap
     */
    public static Config getConfig() {
        if (config == null) {
            generateConfig();
        }
        return config;
    }
}
