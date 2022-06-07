package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public interface PolyMcAsset extends AssetWithDependencies {
    default void write(Path location, Gson gson) throws IOException {
        var writer = new FileOutputStream(location.toFile());
        this.writeToStream(writer, gson);
        writer.close();
        this.writeMetaToStream(() -> new FileOutputStream(Path.of(location + ".mcmeta").toFile()), gson);
    }

    void writeToStream(OutputStream stream, Gson gson) throws IOException;

    /**
     * @param stream A supplier that will get the stream to write to. Only called if this asset actually has a metafile.
     *               The resulting stream will automatically be closed
     */
    default void writeMetaToStream(StreamSupplier stream, Gson gson) throws IOException {

    }

    @FunctionalInterface
    interface StreamSupplier {
        OutputStream get() throws IOException;
    }
}
