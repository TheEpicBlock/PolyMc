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

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.swordglowsblue.artifice.api.Artifice;
import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;

public class AdvancedResourcePackMaker extends ResourcePackMaker{
    protected final Path tempLocation;
    public AdvancedResourcePackMaker(Path buildLocation, Path tempLocation) {
        super(buildLocation);
        this.tempLocation = tempLocation;

        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            Path assets = mod.getPath("assets");
            try {
                Files.copy(assets,tempLocation);
            } catch (IOException e) {
                PolyMc.LOGGER.warning("Failed to get resources from mod " + mod.getMetadata().getId());
            }
        });

        Optional<ModContainer> artifice = FabricLoader.getInstance().getModContainer("artifice");
        if (artifice.isPresent()) {
            Artifice.ASSETS.forEach((artificeResourcePack -> {
                try {
                    artificeResourcePack.dumpResources(tempLocation.toString());
                } catch (IOException e) {
                    PolyMc.LOGGER.warning("Failed to get resources from artifice pack " + artificeResourcePack.getName());
                }
            }));
        }
    }

    /**
     * copies a file from the modId's jar into this resourcepack
     * @param modId
     * @param path example: "asset/testmod/models/item/testitem.json"
     * @return The path to the new file
     */
    private Path copyFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;
//        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
//        if (!modOpt.isPresent()) {
//            PolyMc.LOGGER.warning("Tried to access assets from mod, but it isn't present. Mod ID "+modId);
//            return null;
//        }

//        ModContainer mod = modOpt.get();
        Path filePath = tempLocation.resolve(path);
        Path newLoc = BuildLocation.resolve(path);
        boolean c = newLoc.toFile().mkdirs();
        try {
            return Files.copy(filePath, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PolyMc.LOGGER.warning("Failed to get resource from mod jar '"+modId+"' path: " + path);
        }
        return null;
    }

    private boolean checkFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return false;
//        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
//        if (!modOpt.isPresent()) {
//            return false;
//        }
//
//        ModContainer mod = modOpt.get();
        Path filePath = tempLocation.resolve(path);
        return Files.exists(filePath);
    }

    /**
     * get's a file from the modId's jar's asset folder.
     * @param modId the mod who's assets we're getting from
     * @param path example "asset/testmod/models/item/testitem.json"
     * @return A reader for this file.
     */
    private InputStreamReader getFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;
//        Optional<ModContainer> modOpt = FabricLoader.getInstance().getModContainer(modId);
//        if (!modOpt.isPresent()) {
//            PolyMc.LOGGER.warning("Tried to access assets from mod, but it isn't present. Mod ID "+modId);
//            return null;
//        }
//
//        ModContainer mod = modOpt.get();
        Path filePath = tempLocation.resolve(path);
        try {
            return new InputStreamReader(Files.newInputStream(filePath, StandardOpenOption.READ));
        } catch (IOException e) {
            PolyMc.LOGGER.warning("Failed to get resource from mod jar '"+modId+"' path: " + path);
        }
        return null;
    }
}
