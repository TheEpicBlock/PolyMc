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
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.register.PolyRegistry;
import io.github.theepicblock.polymc.generator.Generator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PolyMc implements ModInitializer {
    private static PolyMap map;
    private static Config config;
    public static final Logger LOGGER = Logger.getLogger("PolyMc");

    @Override
    public void onInitialize() {
        PolyMcCommands.registerCommands();
    }

    /**
     * Builds the poly map, this should only be run when all blocks/items have been registered.
     * This will be called by PolyMc when the worlds are generated.
     * @deprecated this is an internal method you shouldn't call
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void generatePolyMap() {
        PolyRegistry registry = new PolyRegistry();

        //Let mods register polys via the api
        List<PolyMcEntrypoint> entrypoints = FabricLoader.getInstance().getEntrypoints("poly-mc",PolyMcEntrypoint.class);
        for (PolyMcEntrypoint entrypointEntry : entrypoints) {
            entrypointEntry.registerPolys(registry);
        }

        //Auto generate the rest
        Generator.generateMissing(registry);

        map = registry.build();
    }

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
                    LOGGER.warning("error whilst copying over default config. An error trying to load said config will most likely appear soon");
                    e.printStackTrace();
                }
            } else {
                LOGGER.warning("Couldn't copy over default config file. An error trying to load said config will most likely appear soon");
                LOGGER.warning("The modcontainer for 'polymc' couldn't be found.");
                LOGGER.warning("Did someone change the modid in the fabric.mod.json!?");
            }
        }
        Gson gson = new Gson();

        try {
            JsonReader reader = new JsonReader(new FileReader(configFile));
            Config config = gson.fromJson(reader, Config.class);
            PolyMc.config = config;
        } catch (FileNotFoundException e) {
            LOGGER.warning("Couldn't find config file: " + configFile.getPath());
        }
    }

    /**
     * Gets the polymap needed to translate from server items to client items.
     * @throws NullPointerException if you try to access it before the server worlds get initialized
     * @return the PolyMap
     */
    public static PolyMap getMap() {
        if (map == null) {
            throw new NullPointerException("Tried to access the PolyMap before it was initialized");
        }
        return map;
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
