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
package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
public class ResourcePackMaker {
    public static final String ASSETS = "assets/";
    public static final String MODELS = "models/";
    public static final String TEXTURES = "textures/";
    public static final String SOUNDS = "sounds/";
    public static final String BLOCKSTATES = "blockstates/";

    private final static String CLIENT_URL = "https://launcher.mojang.com/v1/objects/060dd014d59c90d723db87f9c9cedb511f374a71/client.jar";
    private static ZipFile clientJar = null;

    protected final Path buildLocation;
    protected final SimpleLogger logger;
    protected final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private final List<Identifier> copiedModels = new ArrayList<>();
    private final Map<Identifier,JsonModel> modelsToSave = new HashMap<>();
    private final Map<Identifier,JsonBlockState> blockStatesToSave = new HashMap<>();

    public ResourcePackMaker(Path buildLocation, SimpleLogger logger) {
        this.buildLocation = buildLocation;
        this.logger = logger;
    }

    /**
     * Get Minecraft's client.jar file
     */
    public static ZipFile getClientJar() {

        if (clientJar != null) {
            return clientJar;
        }

        try {
            Path clientJarPath;
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                clientJarPath = FabricLoader.getInstance().getGameDir().resolve("assets_client.jar");
            } else {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                clientJarPath = Path.of(clientFile);
            }

            if (!clientJarPath.toFile().exists()) {
                URL url = new URL(CLIENT_URL);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                Files.copy(is, clientJarPath);
            }

            clientJar = new ZipFile(clientJarPath.toFile());

            return clientJar;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get an InputStream from the Client jar
     */
    public static InputStream getClientJarInputStream(String path) {
        try {
            return getClientJar().getInputStream(getClientJar().getEntry(path));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if there's a pending model for that id.
     * @param id id to check. Example: "minecraft:item/stick".
     * @return True if the specified id already has a model associated.
     */
    public boolean hasPendingModel(Identifier id) {
        return modelsToSave.containsKey(id);
    }

    /**
     * @see #hasPendingModel(Identifier)
     */
    public boolean hasPendingModel(String modId, String path) {
        return hasPendingModel(new Identifier(modId, path));
    }

    /**
     * @see #hasPendingModel(Identifier)
     */
    public boolean hasPendingModel(String path) {
        return hasPendingModel(new Identifier(Util.MC_NAMESPACE, path));
    }

    /**
     * Replaces the pending model for that id with the provided one.
     * In general it is advised to only use this if {@link #hasPendingModel(Identifier)} is false. Otherwise, use {@link #getPendingModel(Identifier)} and modify it.
     * @param id    the id whose model we should replace. Example: "minecraft:item/stick".
     * @param model model to use for {@code id}
     */
    public void putPendingModel(Identifier id, JsonModel model) {
        modelsToSave.put(id, model);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingModel(String modId, String path, JsonModel model) {
        putPendingModel(new Identifier(modId, path), model);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingModel(String path, JsonModel model) {
        putPendingModel(new Identifier(Util.MC_NAMESPACE, path), model);
    }

    /**
     * Gets the pending model for that Id if it exists, returns {@code null} otherwise.
     * @param id id whose associated model we should return. Example: "minecraft:item/stick".
     * @return The pending model for the specified id. Or {@code null} if there is none.
     */
    public JsonModel getPendingModel(Identifier id) {
        return modelsToSave.get(id);
    }

    /**
     * @see #getPendingModel(Identifier)
     */
    public JsonModel getPendingModel(String modId, String path) {
        return getPendingModel(new Identifier(modId, path));
    }

    /**
     * @see #getPendingModel(Identifier)
     */
    public JsonModel getPendingModel(String path) {
        return getPendingModel(new Identifier(Util.MC_NAMESPACE, path));
    }

    /**
     * Gets a pending model in the item directory using the specified path. If it doesn't exist, it creates a default item model. The model isn't guaranteed to accurately represent all items.
     * @param id example: "minecraft:stick".
     * @return The resulting pending model.
     * @see #getOrDefaultPendingBlockState(String, String)
     */
    public JsonModel getOrDefaultPendingItemModel(Identifier id) {
        return getOrDefaultPendingItemModel(id.getNamespace(), id.getPath());
    }

    /**
     * @see #getOrDefaultPendingBlockState(Identifier)
     */
    public JsonModel getOrDefaultPendingItemModel(String modId, String path) {
        Identifier id = new Identifier(modId, "item/" + path);
        if (hasPendingModel(id)) return getPendingModel(id);

        JsonModel v = new JsonModel();
        v.parent = "item/generated";
        v.textures = new HashMap<>();
        v.textures.put("layer0", "item/" + path);
        putPendingModel(id, v);
        return v;
    }

    /**
     * @see #getOrDefaultPendingBlockState(Identifier)
     */
    public JsonModel getOrDefaultPendingItemModel(String path) {
        return getOrDefaultPendingItemModel(Util.MC_NAMESPACE, path);
    }

    /**
     * Checks if there's a pending blockState for that id.
     * @param id id to check. Example: "minecraft:grass_block".
     * @return True if the specified id already has a blockState associated.
     */
    public boolean hasPendingBlockState(Identifier id) {
        return blockStatesToSave.containsKey(id);
    }

    /**
     * @see #hasPendingModel(Identifier)
     */
    public boolean hasPendingBlockState(String modId, String path) {
        return hasPendingBlockState(new Identifier(modId, path));
    }

    /**
     * @see #hasPendingModel(Identifier)
     */
    public boolean hasPendingBlockState(String path) {
        return hasPendingBlockState(new Identifier(Util.MC_NAMESPACE, path));
    }

    /**
     * Replaces the pending blockState for that id with the provided one.
     * In general it is advised to only use this if {@link #hasPendingBlockState(Identifier)} is false. Otherwise, use {@link #getPendingBlockState(Identifier)} and modify it.
     * @param id         the id whose model we should replace. Example: "minecraft:grass_block".
     * @param blockState blockState to use for {@code id}
     */
    public void putPendingBlockState(Identifier id, JsonBlockState blockState) {
        blockStatesToSave.put(id, blockState);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingBlockState(String modId, String path, JsonBlockState blockState) {
        putPendingBlockState(new Identifier(modId, path), blockState);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingBlockState(String path, JsonBlockState blockState) {
        putPendingBlockState(new Identifier(Util.MC_NAMESPACE, path), blockState);
    }

    /**
     * Gets the pending model for that Id if it exists, returns {@code null} otherwise.
     * @param id id whose associated blockState we should return. Example: "minecraft:grass_block".
     * @return The pending model for the specified id. Or {@code null} if there is none.
     */
    public JsonBlockState getPendingBlockState(Identifier id) {
        return blockStatesToSave.get(id);
    }

    /**
     * @see #getPendingModel(Identifier)
     */
    public JsonBlockState getPendingBlockState(String modId, String path) {
        return getPendingBlockState(new Identifier(modId, path));
    }

    /**
     * @see #getPendingModel(Identifier)
     */
    public JsonBlockState getPendingBlockState(String path) {
        return getPendingBlockState(new Identifier(Util.MC_NAMESPACE, path));
    }

    /**
     * Gets the pending blockState for that id. If it doesn't exist, it creates a default one.
     * @param id example: "minecraft:grass_block".
     * @return The resulting pending blockState.
     */
    public JsonBlockState getOrDefaultPendingBlockState(Identifier id) {
        if (hasPendingBlockState(id)) return getPendingBlockState(id);

        JsonBlockState v = new JsonBlockState();
        blockStatesToSave.put(id, v);
        return v;
    }

    /**
     * @see #getOrDefaultPendingBlockState(Identifier)
     */
    public JsonBlockState getOrDefaultPendingBlockState(String modId, String path) {
        return getOrDefaultPendingBlockState(new Identifier(modId, path));
    }

    /**
     * @see #getOrDefaultPendingBlockState(Identifier)
     */
    public JsonBlockState getOrDefaultPendingBlockState(String path) {
        return getOrDefaultPendingBlockState(new Identifier(Util.MC_NAMESPACE, path));
    }

    /**
     * Imports a mod's entire asset folder.
     * <p>
     * This can be useful for other serverside mods to import their resource pack into PolyMc's system.
     * Do keep in mind that PolyMc has a lot of compatibility functionality for things like custommodeldata. It's recommended to use those to avoid cmd values from conflicting.
     * @param modId mod to import assets from.
     * @see #copyFolder(String, String, String) for more control of what you're copying.
     */
    public void importAssetFolder(String modId) {
        this.copyFolder(modId, "assets", "");
    }

    /**
     * Places the model of this item into this resource pack. Together with everything this model depends on.
     * @param item item whose model we should copy.
     */
    public void copyItemModel(Item item) {
        Identifier id = Registry.ITEM.getId(item);
        copyModel(id.getNamespace(), "item/" + id.getPath());
    }

    /**
     * Copies a model file into this resource pack. Resolving all dependencies on the way.
     * @param modId the mod who owns the model.
     * @param path  path to model. Example "item/testitem".
     * @see #copyModel(Identifier)
     */
    public void copyModel(String modId, String path) {
        if (Util.isNamespaceVanilla(modId)) return;
        //copy the file from the mod (we assume the modid is the same as the item's id)
        Path newFile = copyAsset(modId, MODELS + path + ".json");

        if (newFile == null) return;
        try {
            JsonReader reader = new JsonReader(new FileReader(newFile.toString()));
            JsonModel model = gson.fromJson(reader, JsonModel.class);

            //--------RESOLVE DEPENDENCIES--------
            //resolve textures
            if (model.textures != null) {
                model.textures.forEach((textureRef, id) -> {
                    //textureRef is an internal thing used in the model itself. Not needed to resolve the dependencies
                    Identifier mcId = Identifier.tryParse(id);
                    if (mcId != null) copyTexture(mcId.getNamespace(), mcId.getPath());
                });
            }

            //resolve parent
            if (model.parent != null) {
                Identifier parentId = new Identifier(model.parent);
                copyModel(parentId);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies a model file into this resource pack. Resolving all dependencies on the way.
     * @param id {@code namespace}: the mod who owns the model. {@code path}: path to model. Example "item/testitem".
     * @see #copyModel(String, String)
     */
    public void copyModel(Identifier id) {
        if (!copiedModels.contains(id)) {
            copyModel(id.getNamespace(), id.getPath());
            copiedModels.add(id);
        }
    }

    /**
     * Copies a texture file into this resource pack. Resolving all dependencies on the way.
     * @param modId the mod who owns the texture.
     * @param path  path to model. Example: "item/testtexture".
     */
    public void copyTexture(String modId, String path) {
        copyAsset(modId, TEXTURES + path + ".png");
        String mcMetaPath = TEXTURES + path + ".png.mcmeta";
        if (checkAsset(modId, mcMetaPath)) {
            copyAsset(modId, mcMetaPath);
        }
    }

    /**
     * Copies a sound file into this resource pack.
     * @param modId the mod who owns the texture.
     * @param path  path to sound. Example: "menu_open".
     */
    public void copySound(String modId, String path) {
        copyAsset(modId, SOUNDS + path + ".ogg");
    }

    /**
     * Copies a file from the modId's jar's asset folder to this resource pack.
     * @param modId the mod who owns the asset.
     * @param path  example: "models/item/testitem.json".
     * @return The path to the new file.
     */
    public Path copyAsset(String modId, String path) {
        return copyFile(modId, String.format(ASSETS + "%s/%s", modId, path));
    }

    /**
     * Checks if a mod's jar contains the asset
     * @param modId the mod who owns the asset.
     * @param path  example: "models/item/testitem.json".
     * @return True if the file exists.
     */
    public boolean checkAsset(String modId, String path) {
        return checkFile(modId, String.format(ASSETS + "%s/%s", modId, path));
    }

    /**
     * Gets a file from the modId's jar's asset folder.
     * @param modId the mod who owns the asset.
     * @param path  example: "models/item/testitem.json".
     * @return A reader for this file.
     */
    public InputStreamReader getAsset(String modId, String path) {
        return getFile(modId, String.format(ASSETS + "%s/%s", modId, path));
    }

    /**
     * Copies a folder from {@code modId}'s jar.
     * @param modId       the mod that we need to get a folder from.
     * @param path        example: "assets", "assets/models".
     * @param newLocation example: "assets", "assets/models".
     */
    public void copyFolder(String modId, String path, String newLocation) {
        if (modId.equals("minecraft")) return; //we can't access minecraft resources easily
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            logger.warn(String.format("Tried to access assets from '%s' but it isn't present", modId));
            return;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        Path newLoc = buildLocation.resolve(newLocation);
        boolean c = newLoc.toFile().getParentFile().mkdirs();
        try {
            Util.copyAll(pathInJar, newLoc);
        } catch (IOException e) {
            logger.warn(String.format("Failed to get folder from mod jar '%s' path: %s", modId, path));
        }
    }

    /**
     * Gets a file from the resource pack folder.
     * If you need to get something that isn't in the assets folder, use {@link #getFileDirect(String, String)} instead.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return A reader for this file. Can be null.
     */
    public InputStreamReader getFile(String modId, String path) {
        InputStream stream = this.getFileStream(modId, path);

        if (stream != null) {
            return new InputStreamReader(stream);
        }

        return null;
    }

    /**
     * Gets a file from the resource pack folder.
     * If you need to get something that isn't in the assets folder, use {@link #getFileDirect(String, String)} instead.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return A reader for this file. Can be null.
     */
    public InputStream getFileStream(String modId, String path) {
        return getFileStreamDirect(modId, path);
    }

    /**
     * Gets a file from the resource pack folder.
     * This gets it directly from the jar, even if {@link io.github.theepicblock.polymc.impl.Config.ResourcePackConfig#advancedDiscovery} is set to true.
     * This should only be used if the thing you want to get is not in the assets folder.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return A reader for this file. Can be null.
     */
    public final InputStreamReader getFileDirect(String modId, String path) {
        InputStream stream = this.getFileStreamDirect(modId, path);

        if (stream != null) {
            return new InputStreamReader(stream);
        }

        return null;
    }

    /**
     * Gets a file stream from the resource pack folder.
     * This gets it directly from the jar, even if {@link io.github.theepicblock.polymc.impl.Config.ResourcePackConfig#advancedDiscovery} is set to true.
     * This should only be used if the thing you want to get is not in the assets folder.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return A reader for this file. Can be null.
     */
    public final InputStream getFileStreamDirect(String modId, String path) {
        //if (modId.equals("minecraft")) return null; //we can't access minecraft resources easily
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            logger.warn(String.format("Tried to access assets from '%s' but it isn't present", modId));
            return null;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        try {
            return Files.newInputStream(pathInJar, StandardOpenOption.READ);
        } catch (IOException e) {
            logger.warn(String.format("Failed to get resource from mod jar '%s' path: '%s'", modId, path));
        }
        return null;
    }

    /**
     * Checks if a file exists.
     * If you need to check something that isn't in the assets folder, use {@link #checkFileDirect(String, String)} instead.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return The path to the new file.
     */
    protected boolean checkFile(String modId, String path) {
        return checkFileDirect(modId, path);
    }

    /**
     * Checks if a file exists.
     * This checks it directly from the jar, even if {@link io.github.theepicblock.polymc.impl.Config.ResourcePackConfig#advancedDiscovery} is set to true.
     * This should only be used if the thing you want to check is not in the assets folder.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return If the file exists
     */
    public final boolean checkFileDirect(String modId, String path) {

        if (modId.equals("minecraft")) {
            try {
                return getClientJar().getEntry(path) != null;
            } catch (Exception e) {
                return false;
            }
        }

        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            return false;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        return Files.exists(pathInJar);
    }

    /**
     * Copies a file into this resource pack.
     * If you need to copy something that isn't in the assets folder, use {@link #copyFileDirect(String, String)} instead.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return The path to the new file. Can be null.
     */
    protected Path copyFile(String modId, String path) {
        return this.copyFileDirect(modId, path);
    }

    /**
     * Copies a file into this resource pack.
     * This copies it directly from the jar, even if {@link io.github.theepicblock.polymc.impl.Config.ResourcePackConfig#advancedDiscovery} is set to true.
     * This should only be used if the thing you want to copy is not in the assets folder.
     * @param modId the mod who owns the file.
     * @param path  example: "asset/testmod/models/item/testitem.json".
     * @return The path to the new file. Can be null.
     */
    public final Path copyFileDirect(String modId, String path) {

        if (modId.equals("minecraft")) {
            try {
                return this.writeToPath(path, getClientJarInputStream(path).readAllBytes());
            } catch (Exception e) {
                logger.warn(String.format("Failed to get resource from client jar '%s' path: %s", modId, path));
                return null;
            }
        }

        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            logger.warn(String.format("Tried to access assets from '%s' but it isn't present", modId));
            return null;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        Path newLoc = buildLocation.resolve(path);
        if (Files.exists(newLoc)) {return newLoc;} //Avoid copying twice

        boolean c = newLoc.toFile().getParentFile().mkdirs();
        try {
            return Files.copy(pathInJar, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warn(String.format("Failed to get resource from mod jar '%s' path: %s", modId, path));
            return null;
        }
    }

    /**
     * Write some bytes to a file
     * @param path   The path to the target file
     * @param data   The data to write
     */
    public Path writeToPath(String path, byte[] data) {
        try {
            // Construct the full path inside the asset build location
            Path targetPath = buildLocation.resolve(path);

            // Make sure the parent directory exists
            targetPath.toFile().getParentFile().mkdirs();

            // Write the actual file
            return Files.write(targetPath, data);
        } catch (IOException e) {
            logger.warn(String.format("Failed to write to '%s'", path));
            logger.warn(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Avoids having to create a new Gson for everything.
     * @return A gson!
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Gets the location where assets are being copied to.
     * It is advised not to write directly into this directory unless there is no other method.
     */
    public Path getBuildLocation() {
        return buildLocation;
    }

    public SimpleLogger getLogger() {
        return logger;
    }

    /**
     * Saves all in-memory changes to the disk.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveAll() {
        modelsToSave.forEach((id, model) -> {
            String json = model.toJson(gson);
            Path path = buildLocation.resolve(String.format("%s%s/%s%s.json", ASSETS, id.getNamespace(), MODELS, id.getPath()));
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        blockStatesToSave.forEach((id, blockState) -> {
            String json = gson.toJson(blockState);
            Path path = buildLocation.resolve(String.format("%s%s/%s%s.json", ASSETS, id.getNamespace(), BLOCKSTATES, id.getPath()));
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
