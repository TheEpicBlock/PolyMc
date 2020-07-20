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
import com.swordglowsblue.artifice.common.ClientResourcePackProfileLike;
import com.swordglowsblue.artifice.impl.ArtificeResourcePackImpl;
import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
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
import java.util.function.Consumer;

//TODO organize these classes
public class ResourcePackMaker {
    public static final String MODELS = "models/";
    public static final String TEXTURES = "textures/";
    public static final String BLOCKSTATES = "blockstates/";

    protected final Path BuildLocation;
    protected final Gson gson = new Gson();

    private final List<Identifier> copiedModels = new ArrayList<>();
    private final Map<Identifier,JsonModel> modelsToSave = new HashMap<>();
    private final Map<Identifier,JsonBlockstate> blockStatesToSave = new HashMap<>();

    public ResourcePackMaker(Path buildLocation) {
        BuildLocation = buildLocation;
    }

    /**
     * Add's a minecraft itemmodel to the resourcepack. You can then add additional statements to this model.
     * May not work for all items.
     * @param path example "testitem"
     * @return
     */
    public JsonModel copyMinecraftItemModel(String path) {
        Identifier id = new Identifier("minecraft", "item/"+path);
        JsonModel v = modelsToSave.get(id);
        if (v == null) {
            v = new JsonModel();
            v.parent = "item/generated";
            v.textures = new HashMap<>();
            v.textures.put("layer0","item/"+path);
            modelsToSave.put(id,v);
        }
        return v;
    }

    //TODO document this
    public JsonBlockstate getOrCreateBlockState(Identifier id) {
        if (blockStatesToSave.containsKey(id)) {
            return blockStatesToSave.get(id);
        }
        JsonBlockstate b = new JsonBlockstate();
        blockStatesToSave.put(id,b);
        return b;
    }

    public JsonBlockstate getOrCreateBlockState(String modId, String path) {
        return getOrCreateBlockState(new Identifier(modId,path));
    }

    /**
     * Imports an Artifice resourcepack to be used when getting assets.
     * This is not needed on the client. But it's the only way to support Artifice resourcepacks on servers.
     * This function won't do anything on the client since the pack will automatically be imported there from {@link com.swordglowsblue.artifice.common.ArtificeRegistry#ASSETS}
     * @param pack resourcepack to import
     * @see AdvancedResourcePackMaker#importArtificePack(ArtificeResourcePack)
     */
    public void importArtificePack(ArtificeResourcePack pack) {
        PolyMc.LOGGER.warn("Tried to import Artifice resourcepack '" + pack.getName() + "' but this isn't supported with the default discovery method");
        PolyMc.LOGGER.warn("Please switch to the advancedDiscovery method. See https://github.com/TheEpicBlock/PolyMc/wiki/Config#advanceddiscovery");
    }

    /**
     * Imports an Artifice resourcepack to be used when getting assets.
     * This is not needed on the client. But it's the only way to support Artifice resourcepacks on servers.
     * This function won't do anything on the client since the pack will automatically be imported there from {@link com.swordglowsblue.artifice.common.ArtificeRegistry#ASSETS}
     * @param pack resourcepack to import
     * @see AdvancedResourcePackMaker#importArtificePack(ArtificeResourcePack)
     */
    public void importArtificePack(ClientResourcePackProfileLike pack) {
        if (pack instanceof ArtificeResourcePack) {
            importArtificePack((ArtificeResourcePack)pack);
        } else {
            PolyMc.LOGGER.warn("Failed to get resources from artifice pack");
            PolyMc.LOGGER.warn("Provided resourcepack is not of type ArtificeResourcePack, it is instead " + pack.getClass().getName());
        }
    }

    /**
     * Imports an Artifice resourcepack to be used when getting assets.
     * This is not needed on the client. But it's the only way to support Artifice resourcepacks on servers.
     * This function won't do anything on the client since the pack will automatically be imported there from {@link com.swordglowsblue.artifice.common.ArtificeRegistry#ASSETS}
     * @param pack resourcepack to import
     * @see AdvancedResourcePackMaker#importArtificePack(ArtificeResourcePack)
     */
    public void importArtificePack(Consumer<ArtificeResourcePack.ClientResourcePackBuilder> pack) {
        importArtificePack(new ArtificeResourcePackImpl(ResourceType.CLIENT_RESOURCES,pack));
    }

    /**
     * places the model of this item into this resourcepack. Together with everything this model depends on.
     * @param item
     */
    public void copyItemModel(Item item) {
        Identifier id = Registry.ITEM.getId(item);
        copyModel(id.getNamespace(),"item/"+id.getPath());
    }

    /**
     * copies a model file into this resourcepack. Resolving all dependencies on the way.
     * @param modId mod containing this model
     * @param path path to model. Example "item/testitem"
     * @see #copyModel(Identifier)
     */
    private void copyModel(String modId, String path) {
        //copy the file from the mod (we assume the modid is the same as the item's id)
        Path newFile = copyAssetFromMod(modId,MODELS+path+".json");

        if (newFile == null) return;
        try {
            JsonReader reader = new JsonReader(new FileReader(newFile.toString()));
            JsonModel model = gson.fromJson(reader, JsonModel.class);

            //--------RESOLVE DEPENDENCIES--------
            //resolve textures
            if (model.textures != null) {
                model.textures.forEach((textureRef,id) -> {
                    //textureRef is an internal thing used in the model itself. Not needed to resolve the dependencies
                    Identifier mcId = new Identifier(id);
                    copyTexture(mcId.getNamespace(),mcId.getPath());
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
     * copies a model file into this resourcepack. Resolving all dependencies on the way.
     * @param id {@code namespace}: mod containing this model. {@code path}: path to model. Example "item/testitem"
     * @see #copyModel(String,String)
     */
    public void copyModel(Identifier id) {
        if (!copiedModels.contains(id)) {
            copyModel(id.getNamespace(),id.getPath());
            copiedModels.add(id);
        }
    }

    /**
     * copies a texture file into this resourcepack. Resolving all dependencies on the way.
     * @param modId mod containing this model
     * @param path path to model. Example "item/testtexture"
     */
    public void copyTexture(String modId, String path) {
        copyAssetFromMod(modId, TEXTURES+path+".png");
        String mcMetaPath = TEXTURES+path+".png.mcmeta";
        if (checkForAsset(modId,mcMetaPath)) {
            copyAssetFromMod(modId, mcMetaPath);
        }
    }

    /**
     * copies a file from the modId's jar into this resourcepack
     * @param modId
     * @param path example: "asset/testmod/models/item/testitem.json"
     * @return The path to the new file
     */
    protected Path copyFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;
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

    protected boolean checkFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return false;
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            return false;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        return Files.exists(pathInJar);
    }

    /**
     * get's a file from the modId's jar's asset folder.
     * @param modId the mod who's assets we're getting from
     * @param path example "asset/testmod/models/item/testitem.json"
     * @return A reader for this file.
     */
    protected InputStreamReader getFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;
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
     * copies a file from the modId's jar's asset folder to this resourcepack
     * @param modId the mod who's assets we're getting from
     * @param path example "models/item/testitem.json"
     * @return The path to the new file
     */
    public Path copyAssetFromMod(String modId, String path) {
        return copyFileFromMod(modId, String.format("assets/%s/%s", modId, path));
    }

    /**
     * Checks if a mod's jar contains the asset
     * @param modId the mod who's assets we're checking
     * @param path example "models/item/testitem.json"
     * @return true if the file exists
     */
    public boolean checkForAsset(String modId, String path) {
        return checkFileFromMod(modId, String.format("assets/%s/%s", modId, path));
    }

    /**
     * get's a file from the modId's jar's asset folder.
     * @param modId the mod who's assets we're getting from
     * @param path example "models/item/testitem.json"
     * @return A reader for this file.
     */
    public InputStreamReader getAssetFromMod(String modId, String path) {
        return getFileFromMod(modId, String.format("assets/%s/%s", modId, path));
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Saves all changes that haven't been done yet
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveAll() {
        modelsToSave.forEach((id,model) -> {
            String json = model.toJson(gson);
            Path path = BuildLocation.resolve("assets/"+id.getNamespace()+"/"+MODELS+id.getPath()+".json");
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        blockStatesToSave.forEach((id,blockState) -> {
            String json = gson.toJson(blockState);
            Path path = BuildLocation.resolve("assets/"+id.getNamespace()+"/"+BLOCKSTATES+id.getPath()+".json");
            path.toFile().getParentFile().mkdirs();
            try {
                Files.write(path, json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
