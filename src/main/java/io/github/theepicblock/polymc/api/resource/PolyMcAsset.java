package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Path;

public interface PolyMcAsset extends AssetWithDependencies {
    void write(Path location, Gson gson) throws IOException;
}
