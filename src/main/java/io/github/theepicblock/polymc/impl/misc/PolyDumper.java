package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PolyDumper {
    public static void dumpPolyMap(PolyMap map, String fileName, SimpleLogger logger) throws IOException {
        File polyDumpFile = new File(FabricLoader.getInstance().getGameDir().toFile(), fileName);
        if (polyDumpFile.exists()) {
            boolean a = polyDumpFile.delete();
            if (!a) throw new IOException("Failed to remove old polyMap");
        }
        boolean b = polyDumpFile.createNewFile();
        if (!b) throw new IOException("Couldn't create file");

        try {
            //Write the contents of polyDump to the polyDumpFile
            FileWriter writer = new FileWriter(polyDumpFile);
            writer.write(map.dumpDebugInfo());
            writer.close();
        } catch (IOException e) {
            logger.error("An error occurred whilst trying to generate the polyDump! Please check the console.");
            e.printStackTrace();
        }
    }
}
