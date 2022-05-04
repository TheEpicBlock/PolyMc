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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    public static final Logger LOGGER = LogManager.getLogger("PolyMc-config");
    private static Config config;

    /**
     * Loads the config
     */
    public static void generateConfig() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdirs();
        File configFile = new File(configDir, "polymc.json");

        //pre fill if it doesn't exist yet
        if (!configFile.exists()) {
            Path defaultConfig = getPathFromResources("defaultconfig.json");
            Objects.requireNonNull(defaultConfig);

            try {
                Files.copy(defaultConfig, Paths.get(configFile.getAbsolutePath()));
            } catch (IOException e) {
                LOGGER.warn("error whilst copying over default config. An error trying to load said config will most likely appear soon");
                e.printStackTrace();
            }
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        try {
            JsonReader reader = new JsonReader(new FileReader(configFile));
            JsonElement configJson = gson.fromJson(reader, JsonElement.class);
            Config configobj = gson.fromJson(configJson, Config.class);

            //Update the config
            if (configobj.getConfigVersion() < Config.LATEST_VERSION) {
                int cVersion = configobj.getConfigVersion();
                LOGGER.info("Updating config from v" + cVersion + " to v" + Config.LATEST_VERSION);

                Path updatesPath = getPathFromResources("config_update.json");
                Objects.requireNonNull(updatesPath);
                JsonReader uReader = new JsonReader(new InputStreamReader(Files.newInputStream(updatesPath)));
                JsonObject updates = gson.fromJson(uReader, JsonObject.class);

                for (int i = cVersion + 1; i <= Config.LATEST_VERSION; i++) {
                    if (cVersion == 0) {
                        LOGGER.error("PolyMc: Config file seems to be corrupt. You might need to delete it");
                    }
                    try {
                        update(i, configJson.getAsJsonObject(), updates);
                    } catch (Exception e) {
                        LOGGER.warn("failed to update config to v" + i);
                        e.printStackTrace();
                    }
                }

                //write updated config to file and read it again
                FileWriter configWriter = new FileWriter(configFile);
                configWriter.write(gson.toJson(configJson));
                configWriter.close();
                configobj = gson.fromJson(configJson, Config.class);
            }
            ConfigManager.config = configobj;
        } catch (FileNotFoundException e) {
            LOGGER.warn("Couldn't find config file: " + configFile.getPath());
            LOGGER.warn(e);
        } catch (IOException e) {
            LOGGER.warn("failed to update config");
            LOGGER.warn(e);
        }
    }

    private static Path getPathFromResources(String path) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("polymc");
        if (container.isPresent()) {
            ModContainer polymcContainer = container.get();
            return polymcContainer.getPath(path);
        } else {
            LOGGER.warn("The modcontainer for 'polymc' couldn't be found.");
            LOGGER.warn("Did someone change the modid in the fabric.mod.json!?");
            LOGGER.warn("The server will probably crash due to a NullPointer exception now");
            return null;
        }
    }

    private static void update(int v, JsonObject config, JsonObject updates) {
        JsonObject update = updates.getAsJsonObject(String.valueOf(v));

        //process additions
        JsonObject add = update.getAsJsonObject("add");
        if (add != null) {
            for (Map.Entry<String,JsonElement> e : add.entrySet()) {
                JsonElement element = e.getValue();
                List<String> path = new LinkedList<>(Arrays.asList(e.getKey().split("\\.")));
                String last = path.remove(path.size() - 1);

                if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    traverse(config, path).add(last, obj);
                } else if (element.isJsonArray()) {
                    JsonArray arr = element.getAsJsonArray();
                    JsonObject subject = traverse(config, path);
                    if (subject.has(last)) {
                        subject.getAsJsonArray(last).addAll(arr);
                    } else {
                        subject.add(last, arr);
                    }
                } else if (element.isJsonNull() || element.isJsonPrimitive()) {
                    traverse(config, path).add(last, element);
                }
            }
        }

        //process removals
        JsonArray remove = update.getAsJsonArray("remove");
        if (remove != null) {
            for (JsonElement e : remove) {
                List<String> path = new LinkedList<>(Arrays.asList(e.getAsString().split("\\.")));
                String last = path.remove(path.size() - 1);
                if (path.size() == 0) {
                    config.remove(last);
                    continue;
                }
                String secondLast = path.remove(path.size() - 1);

                JsonElement elementToRemoveFrom = traverse(config, path).get(secondLast);
                if (elementToRemoveFrom.isJsonObject()) {
                    elementToRemoveFrom.getAsJsonObject().remove(last);
                } else if (elementToRemoveFrom.isJsonArray()) {
                    elementToRemoveFrom.getAsJsonArray().remove(new JsonPrimitive(last));
                }
            }
        }
        config.addProperty("configVersion", v);
    }

    private static JsonObject traverse(JsonObject obj, List<String> toTraverse) {
        JsonObject ret = obj;
        for (String s : toTraverse) {
            ret = ret.getAsJsonObject(s);
        }
        return ret;
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
