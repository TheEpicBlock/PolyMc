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

import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import com.swordglowsblue.artifice.common.ArtificeRegistry;
import com.swordglowsblue.artifice.impl.ArtificeResourcePackImpl;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.ResourceType;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class copies all assets into a temp folder. Then uses that to generate the resource pack, instead of getting the assets straight from the jars.
 * This is slower, but could help with finding assets.
 */
public class AdvancedResourcePackMaker extends ResourcePackMaker {
    protected final Path tempLocation;

    public AdvancedResourcePackMaker(Path buildLocation, Path tempLocation, SimpleLogger logger) {
        super(buildLocation, logger);
        this.tempLocation = tempLocation;

        //Get all assets from all mods and copy it into a temporary location
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            Path assets = mod.getPath("assets");
            if (!Files.exists(assets)) return;
            try {
                Util.copyAll(assets,tempLocation);
            } catch (IOException e) {
                PolyMc.LOGGER.warn("Failed to get resources from mod " + mod.getMetadata().getId());
                e.printStackTrace();
            }
        });

        //Artifice provides a list with virtual resource packs. But it only exists on the client. We can import them to use the assets
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Optional<ModContainer> artifice = FabricLoader.getInstance().getModContainer("artifice");
            if (artifice.isPresent()) {
                ArtificeRegistry.ASSETS.forEach((this::importArtificePack));
            }
        }
    }

    @Override
    public void importArtificePack(Object pack) {
        if (pack instanceof ArtificeResourcePack) {
            ArtificeResourcePack aPack = (ArtificeResourcePack)pack;
            try {
                Path artLoc = FabricLoader.getInstance().getGameDir().relativize(tempLocation);
                aPack.dumpResources(artLoc.toString());
            } catch (IOException e) {
                PolyMc.LOGGER.warn(String.format("Failed to get resources from artifice pack '%s'", aPack.getName()));
                PolyMc.LOGGER.warn(e);
            }
        }
        if (pack instanceof Consumer) {
            //noinspection unchecked
            importArtificePack(new ArtificeResourcePackImpl(ResourceType.CLIENT_RESOURCES, (Consumer<ArtificeResourcePack.ClientResourcePackBuilder>)pack));
        }
    }

    @Override
    protected Path copyFile(String modId, String path) {
        if (modId.equals("minecraft")) return null;

        Path filePath = tempLocation.resolve(path);
        Path newLoc = buildLocation.resolve(path);
        //noinspection ResultOfMethodCallIgnored
        newLoc.toFile().getParentFile().mkdirs();
        try {
            return Files.copy(filePath, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PolyMc.LOGGER.warn(String.format("Failed to get resource from mod '%s' path: '%s'", modId, path));
        }
        return null;
    }

    @Override
    protected boolean checkFile(String modId, String path) {
        if (modId.equals("minecraft")) return false;

        Path filePath = tempLocation.resolve(path);
        return Files.exists(filePath);
    }

    @Override
    protected InputStreamReader getFile(String modId, String path) {
        if (modId.equals("minecraft")) return null;

        Path filePath = tempLocation.resolve(path);
        try {
            return new InputStreamReader(Files.newInputStream(filePath, StandardOpenOption.READ));
        } catch (IOException e) {
            PolyMc.LOGGER.warn(String.format("Failed to get resource from mod '%s' path: '%s'", modId, path));
        }
        return null;
    }

    @Override
    public void saveAll() {
        super.saveAll();

        try {
            FileUtils.deleteDirectory(tempLocation.toFile());
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Couldn't delete temporary file");
            PolyMc.LOGGER.warn(e);
        }
    }
}
