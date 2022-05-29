package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface PolyMcAsset extends AssetWithDependencies {
    /**
     * @deprecated please don't overwrite this method. In 5.0.0 this method will have a default implementation, and you'll be required to overwrite {@link #writeToStream(OutputStream, Gson)}
     */
    @Deprecated
    void write(Path location, Gson gson) throws IOException;

    default void writeToStream(OutputStream stream, Gson gson) throws IOException {
        // Backwards compatible method
        var tempDir = Files.createTempDirectory(null);
        var file = tempDir.resolve("file.tmp");
        this.write(file, gson);
        Files.copy(file, stream);
    }

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
