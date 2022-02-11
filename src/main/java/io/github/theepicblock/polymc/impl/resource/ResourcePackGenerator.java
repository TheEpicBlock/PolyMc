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

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ResourcePackGenerator {
    // TODO event

    public static void cleanAndWrite(PolyMcResourcePack pack, String directory, SimpleLogger logger) {

        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path resourcePath = gameDir.resolve(directory).toAbsolutePath();
        resourcePath.toFile().mkdir();

        //Clear up the assets folder
        File assetsFolder = resourcePath.resolve("assets").toFile();
        if (assetsFolder.exists() && assetsFolder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(assetsFolder);
            } catch (IOException e) {
                logger.warn("Couldn't delete the assets folder. There may still be some unneeded files in there");
            }
        }

        pack.write(resourcePath, logger);
    }

    /**
     * @deprecated use {@link PolyMap#generateResourcePack(SimpleLogger)} together with {@link #cleanAndWrite(PolyMcResourcePack, String, SimpleLogger)} or {@link PolyMcResourcePack#write(Path, SimpleLogger)}
     */
    @Deprecated
    public static void generate(PolyMap map, String directory, SimpleLogger logger) {
        var pack = map.generateResourcePack(logger);
        if (pack == null) throw new NullPointerException("PolyMap doesn't support resource generation");
        cleanAndWrite(pack, directory, logger);
    }
}
