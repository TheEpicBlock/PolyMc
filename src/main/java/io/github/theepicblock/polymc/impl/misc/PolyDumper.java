package io.github.theepicblock.polymc.impl.misc;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PolyDumper {
    public static void dumpPolyMap(PolyMap map, String fileName, SimpleLogger logger) throws IOException {
        StringBuilder polyDump = new StringBuilder();
        polyDump.append("###########\n## ITEMS ##\n###########\n");
        map.getItemPolys().forEach((item, poly) -> {
            addDebugProviderToDump(polyDump, item, item.getTranslationKey(), poly, logger);
        });
        polyDump.append("############\n## BLOCKS ##\n############\n");
        map.getBlockPolys().forEach((block, poly) -> {
            addDebugProviderToDump(polyDump, block, block.getTranslationKey(), poly, logger);
        });

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
            writer.write(polyDump.toString());
            writer.close();
        } catch (IOException e) {
            logger.error("An error occurred whilst trying to generate the polyDump! Please check the console.");
            e.printStackTrace();
        }
    }

    private static <T> void addDebugProviderToDump(StringBuilder b, T object, String key, DebugInfoProvider<T> poly, SimpleLogger logger) {
        b.append(Util.expandTo(key, 45));
        b.append(" --> ");
        b.append(Util.expandTo(poly.getClass().getName(), 60));
        try {
            String info = poly.getDebugInfo(object);
            if (info != null) {
                b.append("|");
                b.append(info);
            }
        } catch (Exception e) {
            logger.info(String.format("Error whilst getting debug info from '%s' which is registered to '%s'", poly.getClass().getName(), key));
            e.printStackTrace();
        }
        b.append("\n");
    }
}
