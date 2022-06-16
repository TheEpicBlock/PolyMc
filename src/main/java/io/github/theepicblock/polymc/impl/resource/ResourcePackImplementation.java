package io.github.theepicblock.polymc.impl.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class ResourcePackImplementation implements PolyMcResourcePack {
    private final Map<String, Map<String, PolyMcAsset>> assets = new TreeMap<>();
    private final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();

    @Override
    public void importRequirements(ModdedResources input, AssetWithDependencies asset, SimpleLogger logger) {
        asset.importRequirements(input, this, logger);
    }

    @Override
    public void setAsset(String namespace, String path, PolyMcAsset asset) {
        var perNamespaceMap = assets.computeIfAbsent(namespace, (v) -> new TreeMap<>());
        perNamespaceMap.put(path, asset);
    }

    @Override
    public PolyMcAsset getAsset(String namespace, String path) {
        var perNamespaceMap = assets.get(namespace);
        if (perNamespaceMap == null) return null;
        return perNamespaceMap.get(path);
    }

    @Override
    public void write(Path location, SimpleLogger logger) {
        var assetsFolder = location.resolve(ResourceConstants.ASSETS);

        this.forEachAsset((namespace, path, asset) -> {
            var destination = assetsFolder.resolve(namespace).resolve(path);
            destination.getParent().toFile().mkdirs();
            try {
                asset.write(destination, this.getGson());
            } catch (IOException e) {
                logger.error("Error writing to "+destination);
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("Unknown error whilst writing to "+destination);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void forEachAsset(TriConsumer<String,String,PolyMcAsset> consumer) {
        assets.forEach((namespace, innerMap) -> innerMap.forEach((path, asset) -> consumer.accept(namespace, path, asset)));
    }

    @Override
    public @NotNull Gson getGson() {
        return this.gson;
    }
}
