package io.github.theepicblock.polymc.impl.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public abstract class PolyMcAssetBase implements PolyMcAsset {
    public void write(Path location, Gson gson) throws IOException {
        var writer = new FileOutputStream(location.toFile());
        this.writeToStream(writer, gson);
        writer.close();
        this.writeMetaToStream(() -> new FileOutputStream(Path.of(location + ".mcmeta").toFile()), gson);
    }

    public abstract void writeToStream(OutputStream stream, Gson gson) throws IOException;
}
