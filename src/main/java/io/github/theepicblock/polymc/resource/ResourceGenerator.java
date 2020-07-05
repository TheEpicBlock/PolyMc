package io.github.theepicblock.polymc.resource;

import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class ResourceGenerator {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generate() {
        File gameDir = FabricLoader.getInstance().getGameDirectory();
        File resourceDir = new File(gameDir, "resource");
        resourceDir.mkdirs();
        Path path = resourceDir.toPath().toAbsolutePath();
        ResourcePackMaker pack = new ResourcePackMaker(path);
        PolyMc.getMap().getItemPolys().forEach((item, itemPoly) -> {
            try {
                itemPoly.AddToResourcePack(item, pack);
            } catch (Exception e) {
                PolyMc.LOGGER.warning("Exception whilst generating resources for " + item.getTranslationKey());
                e.printStackTrace();
            }
        });

        pack.saveAll();
    }
}
