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
package io.github.theepicblock.polymc.resource;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;

//TODO organize these classes
public class ResourcePackMaker {
    public static final String ASSETS = "assets/";
    public static final String MODELS = "models/";
    public static final String TEXTURES = "textures/";
    public static final String BLOCKSTATES = "blockstates/";

    protected final Path BuildLocation;
    protected final Gson gson = new Gson();

    private final List<Identifier> copiedModels = new ArrayList<>();
    private final Map<Identifier,JsonModel> modelsToSave = new HashMap<>();
    private final Map<Identifier, JsonBlockState> blockStatesToSave = new HashMap<>();

    public ResourcePackMaker(Path buildLocation) {
        BuildLocation = buildLocation;
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
        return hasPendingModel(new Identifier(modId,path));
    }

    /**
     * Replaces the pending model for that id with the provided one.
     * In general it is advised to only use this if {@link #hasPendingModel(Identifier)} is false. Otherwise, use {@link #getPendingModel(Identifier)} and modify it.
     * @param id the id whose model we should replace. Example: "minecraft:item/stick".
     * @param model
     */
    public void putPendingModel(Identifier id, JsonModel model) {
        modelsToSave.put(id, model);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingModel(String modId, String path, JsonModel model) {
        putPendingModel(new Identifier(modId,path), model);
    }

    /**
     * Get's the pending model for that Id if it exists, returns {@code null} otherwise.
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
        return getPendingModel(new Identifier(modId,path));
    }

    /**
     * Gets a pending model in the item directory using the specified path. If it doesn't exist, it creates a default item model. The model isn't guaranteed to accurately represent all items.
     * @param path example: "testitem".
     * @return The resulting pending model.
     */
    public JsonModel getOrDefaultPendingItemModel(String path) {
        Identifier id = new Identifier("minecraft", "item/"+path);
        if (hasPendingModel(id)) return getPendingModel(id);

        JsonModel v = new JsonModel();
        v.parent = "item/generated";
        v.textures = new HashMap<>();
        v.textures.put("layer0","item/"+path);
        putPendingModel(id,v);
        return v;
    }

    /**
     * Checks if there's a pending blockState for that id.
     * @param id id to check. Example: "minecraft:grass_block".
     * @return True if the specified id already has a blockState associated.
     */
    public boolean hasPendingBlockState(Identifier id) {
        return modelsToSave.containsKey(id);
    }

    /**
     * @see #hasPendingModel(Identifier)
     */
    public boolean hasPendingBlockState(String modId, String path) {
        return hasPendingBlockState(new Identifier(modId,path));
    }

    /**
     * Replaces the pending blockState for that id with the provided one.
     * In general it is advised to only use this if {@link #hasPendingBlockState(Identifier)} is false. Otherwise, use {@link #getPendingBlockState(Identifier)} and modify it.
     * @param id the id whose model we should replace. Example: "minecraft:grass_block".
     * @param blockState blockState to use for {@code id}
     */
    public void putPendingBlockState(Identifier id, JsonBlockState blockState) {
        blockStatesToSave.put(id, blockState);
    }

    /**
     * @see #putPendingModel(Identifier, JsonModel)
     */
    public void putPendingBlockState(String modId, String path, JsonBlockState blockState) {
        putPendingBlockState(new Identifier(modId,path), blockState);
    }

    /**
     * Get's the pending model for that Id if it exists, returns {@code null} otherwise.
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
        return getPendingBlockState(new Identifier(modId,path));
    }
    
    /**
     * Gets the pending blockState for that id. If it doesn't exist, it creates a default one.
     * @param id example: "minecraft:grass_block".
     * @return The resulting pending blockState.
     */
    public JsonBlockState getOrDefaultPendingBlockState(Identifier id) {
        if (hasPendingBlockState(id)) return getPendingBlockState(id);
        
        JsonBlockState v = new JsonBlockState();
        blockStatesToSave.put(id,v);
        return v;
    }

    /**
     * @see #getOrDefaultPendingBlockState(Identifier)
     */
    public JsonBlockState getOrDefaultPendingBlockState(String modId, String path) {
        return getOrDefaultPendingBlockState(new Identifier(modId,path));
    }

    /**
     * Imports an Artifice resourcepack to be used when getting assets.
     * This is not needed on the client. But it's the only way to support Artifice resourcepacks on servers.
     * This function won't do anything on the client since the pack will automatically be imported there from {@link com.swordglowsblue.artifice.common.ArtificeRegistry#ASSETS}.
     * @param pack resourcepack to import.
     * @see AdvancedResourcePackMaker#importArtificePack(Object)
     */
    public void importArtificePack(Object pack) {
        String packname = "unknown:unknown";
        if (pack instanceof ArtificeResourcePack) {
            packname = ((ArtificeResourcePack)pack).getName();
        }
        PolyMc.LOGGER.warn("Tried to import Artifice resourcepack '" + packname + "' but this isn't supported with the default discovery method");
        PolyMc.LOGGER.warn("Please switch to the advancedDiscovery method. See https://github.com/TheEpicBlock/PolyMc/wiki/ModCompat#artifice");
    }

    /**
     * Places the model of this item into this resourcepack. Together with everything this model depends on.
     * @param item
     */
    public void copyItemModel(Item item) {
        Identifier id = Registry.ITEM.getId(item);
        copyModel(id.getNamespace(),"item/"+id.getPath());
    }

    /**
     * Copies a model file into this resourcepack. Resolving all dependencies on the way.
     * @param modId the mod who owns the model.
     * @param path path to model. Example "item/testitem".
     * @see #copyModel(Identifier)
     */
    private void copyModel(String modId, String path) {
        if (Util.isNamespaceVanilla(modId)) return;
        //copy the file from the mod (we assume the modid is the same as the item's id)
        Path newFile = copyAsset(modId,MODELS+path+".json");

        if (newFile == null) return;
        try {
            JsonReader reader = new JsonReader(new FileReader(newFile.toString()));
            JsonModel model = gson.fromJson(reader, JsonModel.class);

            //--------RESOLVE DEPENDENCIES--------
            //resolve textures
            if (model.textures != null) {
                model.textures.forEach((textureRef,id) -> {
                    //textureRef is an internal thing used in the model itself. Not needed to resolve the dependencies
                    Identifier mcId = Identifier.tryParse(id);
                    if (mcId != null) copyTexture(mcId.getNamespace(),mcId.getPath());
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
     * Copies a model file into this resourcepack. Resolving all dependencies on the way.
     * @param id {@code namespace}: the mod who owns the model. {@code path}: path to model. Example "item/testitem".
     * @see #copyModel(String,String)
     */
    public void copyModel(Identifier id) {
        if (!copiedModels.contains(id)) {
            copyModel(id.getNamespace(),id.getPath());
            copiedModels.add(id);
        }
    }

    /**
     * Copies a texture file into this resourcepack. Resolving all dependencies on the way.
     * @param modId the mod who owns the texture.
     * @param path path to model. Example: "item/testtexture".
     */
    public void copyTexture(String modId, String path) {
        copyAsset(modId, TEXTURES+path+".png");
        String mcMetaPath = TEXTURES+path+".png.mcmeta";
        if (checkAsset(modId,mcMetaPath)) {
            copyAsset(modId, mcMetaPath);
        }
    }

    /**
     * Copies a file from the modId's jar's asset folder to this resourcepack.
     * @param modId the mod who owns the asset.
     * @param path example: "models/item/testitem.json".
     * @return The path to the new file.
     */
    public Path copyAsset(String modId, String path) {
        return copyFile(modId, String.format(ASSETS+"%s/%s", modId, path));
    }

    /**
     * Checks if a mod's jar contains the asset
     * @param modId the mod who owns the asset.
     * @param path example: "models/item/testitem.json".
     * @return True if the file exists.
     */
    public boolean checkAsset(String modId, String path) {
        return checkFile(modId, String.format(ASSETS+"%s/%s", modId, path));
    }

    /**
     * Get's a file from the modId's jar's asset folder.
     * @param modId the mod who owns the asset.
     * @param path example: "models/item/testitem.json".
     * @return A reader for this file.
     */
    public InputStreamReader getAsset(String modId, String path) {
        return getFile(modId, String.format(ASSETS+"%s/%s", modId, path));
    }

    /**
     * Get's a file from the resourcepack folder.
     * Unless the resourcepackConfig is set to advanced, this can be used to pull other stuff from the mod jar too.
     * @param modId the mod who owns the file.
     * @param path example: "asset/testmod/models/item/testitem.json".
     * @return A reader for this file. Can be null.
     */
    protected InputStreamReader getFile(String modId, String path) {
        if (modId.equals("minecraft")) return null; //we can't access minecraft resources easily
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            PolyMc.LOGGER.warn("Tried to access assets from mod, but it isn't present. Mod ID "+modId);
            return null;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        try {
            return new InputStreamReader(Files.newInputStream(pathInJar, StandardOpenOption.READ));
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Failed to get resource from mod jar '"+modId+"' path: " + path);
        }
        return null;
    }

    /**
     * Checks if a file exists.
     * Unless the resourcepackConfig is set to advanced, this can be used to pull other stuff from the mod jar too.
     * @param modId the mod who owns the file.
     * @param path example: "asset/testmod/models/item/testitem.json".
     * @return The path to the new file.
     */
    protected boolean checkFile(String modId, String path) {
        if (modId.equals("minecraft")) return false; //we can't access minecraft resources easily
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            return false;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        return Files.exists(pathInJar);
    }

    /**
     * Copies a file into this resourcepack.
     * Unless the resourcepackConfig is set to advanced, this can be used to pull other stuff from the mod jar too.
     * @param modId the mod who owns the file.
     * @param path example: "asset/testmod/models/item/testitem.json".
     * @return The path to the new file. Can be null.
     */
    protected Path copyFile(String modId, String path) {
        if (modId.equals("minecraft")) return null; //we can't access minecraft resources easily
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            PolyMc.LOGGER.warn("Tried to access assets from mod, but it isn't present. Mod ID "+modId);
            return null;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        Path newLoc = BuildLocation.resolve(path);
        boolean c = newLoc.toFile().getParentFile().mkdirs();
        try {
            return Files.copy(pathInJar, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Failed to get resource from mod jar '"+modId+"' path: " + path);
        }
        return null;
    }

    /**
     * Avoids having to create a new Gson for everything.
     * @return A gson!
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Saves all in-memory changes to the disk.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveAll() {
        modelsToSave.forEach((id,model) -> {
            String json = model.toJson(gson);
            Path path = BuildLocation.resolve(ASSETS+id.getNamespace()+"/"+MODELS+id.getPath()+".json");
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        blockStatesToSave.forEach((id,blockState) -> {
            String json = gson.toJson(blockState);
            Path path = BuildLocation.resolve(ASSETS+id.getNamespace()+"/"+BLOCKSTATES+id.getPath()+".json");
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
