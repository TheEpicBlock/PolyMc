package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TextureAsset implements PolyMcAsset {
    private final InputStream texture;
    private final InputStream mcmeta;

    public TextureAsset(InputStream inner, InputStream mcmeta) {
        this.texture = inner;
        this.mcmeta = mcmeta;
    }

    @Override
    public void write(Path location, Gson gson) throws IOException {
        Files.copy(texture, location, StandardCopyOption.REPLACE_EXISTING);
        var metaPath = Path.of(location + ".mcmeta");
        Files.copy(mcmeta, metaPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
