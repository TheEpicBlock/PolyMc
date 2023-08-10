package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
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
        var success = this.cacheRoot.toFile().mkdirs();
        if (!success) {
            PolyMc.LOGGER.warn("Failed to create asm cache dir ("+cacheRoot+"). May lead to further errors down the line");
        }
    }

    public ExecutionGraphNode analyze(EntityType<?> entity) throws MethodExecutor.VmException {
        // Don't worry, this code is temporary™
        var entityCacheFile = getFile(entity);
        if (Files.exists(entityCacheFile)) {
            try (var stream = new ObjectInputStream(new FileInputStream(entityCacheFile.toFile()))) {
                return (ExecutionGraphNode)stream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        var executionResults = rendererAnalyzer.analyze(entity);
        try (var stream = new ObjectOutputStream(new FileOutputStream(entityCacheFile.toFile()))) {
            stream.writeObject(executionResults);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return executionResults;
    }

    public Path getFile(EntityType<?> entity) {
        var id = Registries.ENTITY_TYPE.getId(entity);
        return cacheRoot.resolve(id.toUnderscoreSeparatedString()+".bin");
    }
}
