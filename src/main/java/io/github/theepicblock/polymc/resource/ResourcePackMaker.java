package io.github.theepicblock.polymc.resource;

import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ResourcePackMaker {
    private final Path BuildLocation;

    public ResourcePackMaker(Path buildLocation) {
        BuildLocation = buildLocation;
    }

    public void copyItemModel(Item item) {
        Identifier id = Registry.ITEM.getId(item);
        copyAssetFromMod(id.getNamespace(),"models/item/"+id.getPath()+".json");
    }

    /**
     * Get's a file from the modId's jar
     * @param modId
     * @param file example: "asset/testmod/models/item/testitem.json"
     */
    public void copyFileFromMod(String modId, String file) {
        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
        if (!modOpt.isPresent()) {
            PolyMc.LOGGER.warning("Mod not present whilst trying to get it's assets. Mod ID "+modId);
            return;
        }

        ModContainer mod = modOpt.get();
        Path path = mod.getPath(file);
        Path newLoc = BuildLocation.resolve(file);
        boolean c = newLoc.toFile().mkdirs();
        try {
            Files.copy(path, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets a file from the modId's jar's asset folder
     * @param modId
     * @param asset example "models/item/testitem.json"
     */
    public void copyAssetFromMod(String modId, String asset) {
        copyFileFromMod(modId, String.format("assets/%s/%s", modId, asset));
    }
}
