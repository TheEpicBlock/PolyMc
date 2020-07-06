package io.github.theepicblock.polymc.resource;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

//TODO organize these classes
public class ResourcePackMaker {
    private static final String MODELS = "models/";
    private static final String TEXTURES = "textures/";

    private final Path BuildLocation;
    private final Gson gson = new Gson();

    private final Map<Identifier,JsonModel> modelsToSave = new HashMap<>();

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
     */
    public void copyModel(String modId, String path) {
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
                copyModel(parentId.getNamespace(), parentId.getPath());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * copies a texture file into this resourcepack. Resolving all dependencies on the way.
     * @param modId mod containing this model
     * @param path path to model. Example "item/testtexture"
     */
    public void copyTexture(String modId, String path) {
        copyAssetFromMod(modId, TEXTURES+path+".png");
    }

    /**
     * Get's a file from the modId's jar
     * @param modId
     * @param path example: "asset/testmod/models/item/testitem.json"
     * @return The path to the new file
     */
    public Path copyFileFromMod(String modId, String path) {
        if (modId == "minecraft") return null;
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            PolyMc.LOGGER.warning("Mod not present whilst trying to get it's assets. Mod ID "+modId);
            return null;
        }

        ModContainer mod = modOpt.get();
        Path pathInJar = mod.getPath(path);
        Path newLoc = BuildLocation.resolve(path);
        boolean c = newLoc.toFile().mkdirs();
        try {
            return Files.copy(pathInJar, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * gets a file from the modId's jar's asset folder
     * @param modId
     * @param path example "models/item/testitem.json"
     * @return The path to the new file
     */
    public Path copyAssetFromMod(String modId, String path) {
        return copyFileFromMod(modId, String.format("assets/%s/%s", modId, path));
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
    }
}
