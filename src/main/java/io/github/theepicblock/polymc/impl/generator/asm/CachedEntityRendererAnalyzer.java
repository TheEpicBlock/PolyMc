package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class CachedEntityRendererAnalyzer {
    public static final SharedValuesKey<CachedEntityRendererAnalyzer> KEY = new SharedValuesKey<>(CachedEntityRendererAnalyzer::new, null);

    private final EntityRendererAnalyzer rendererAnalyzer;
    private final Path cacheRoot;

    public CachedEntityRendererAnalyzer(PolyRegistry registry) {
        this.rendererAnalyzer = registry.getSharedValues(EntityRendererAnalyzer.KEY);
        var gameDir = FabricLoader.getInstance().getGameDir();
        this.cacheRoot = gameDir.resolve(".cache").resolve(PolyMc.MODID).resolve("entity_asm");
        try {
            Files.createDirectories(this.cacheRoot);
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Failed to create asm cache dir ("+cacheRoot+"). May lead to further errors down the line");
            e.printStackTrace();
        }
    }

    public ExecutionGraphNode analyze(EntityType<?> entity) throws MethodExecutor.VmException {
        if (entity.getTranslationKey().contains("snail")) return null;

        var entityCacheFile = getFile(entity);
        if (Files.exists(entityCacheFile)) {
            try (var stream = new BufferedInputStream(new FileInputStream(entityCacheFile.toFile()))) {
                var buf = PacketByteBufs.create();
                buf.writeBytes(stream.readAllBytes());
                return ExecutionGraphNode.read(buf);
            } catch (IOException e) {
                PolyMc.LOGGER.warn("Couldn't read "+entityCacheFile);
                e.printStackTrace();
            } catch (Throwable t) {
                PolyMc.LOGGER.warn("Couldn't read "+entityCacheFile+", trying to delete it");
                entityCacheFile.toFile().delete();
                t.printStackTrace();
            }
        }

        var executionResults = rendererAnalyzer.analyze(entity);
        try (var stream = new ObjectOutputStream(new FileOutputStream(entityCacheFile.toFile()))) {
            var buf = PacketByteBufs.create();
            executionResults.write(buf);
            buf.readBytes(stream, buf.readableBytes());
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            entityCacheFile.toFile().delete();
            throw new RuntimeException(e);
        }
        return executionResults;
    }

    public Path getFile(EntityType<?> entity) {
        var id = Registries.ENTITY_TYPE.getId(entity);
        return cacheRoot.resolve(id.toUnderscoreSeparatedString()+".par");
    }
}
