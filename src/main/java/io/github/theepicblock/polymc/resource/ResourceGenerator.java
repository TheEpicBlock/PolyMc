package io.github.theepicblock.polymc.resource;

import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ResourceGenerator {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generate() {
        File gameDir = FabricLoader.getInstance().getGameDirectory();
        File resourceDir = new File(gameDir, "resource");
        resourceDir.mkdirs();
        Path path = resourceDir.toPath().toAbsolutePath();
        ResourcePackMaker pack = new ResourcePackMaker(path);

        //Hooks for all itempolys
        PolyMc.getMap().getItemPolys().forEach((item, itemPoly) -> {
            try {
                itemPoly.AddToResourcePack(item, pack);
            } catch (Exception e) {
                PolyMc.LOGGER.warning("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        //Get all lang files from all mods
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            Path langPath = mod.getPath(String.format("assets/%s/lang", modId));
            if (!Files.exists(langPath)) continue;
            try {
                Stream<Path> pathStream = Files.list(langPath);
                pathStream.forEach((langFile) -> {
                    pack.copyAssetFromMod(modId, "lang/"+langPath.relativize(langFile));
                });
            } catch (IOException e) {
                PolyMc.LOGGER.warning("Exception whilst copying lang files from " + modId);
                e.printStackTrace();
            }
        }

        pack.saveAll();
    }
}
