package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.impl.resource.PolyMcAssetBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextureAsset extends PolyMcAssetBase implements PolyMcAsset {
    private final @NotNull InputStream texture;
    private final @Nullable InputStream mcmeta;

    public TextureAsset(@NotNull InputStream inner, @Nullable InputStream mcmeta) {
        this.texture = inner;
        this.mcmeta = mcmeta;
    }

    public @NotNull InputStream getTexture() {
        return texture;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        texture.transferTo(stream);
        texture.close(); // TODO take a proper look at where things are closed
    }

    @Override
    public void writeMetaToStream(StreamSupplier streamS, Gson gson) throws IOException {
        if (mcmeta != null) {
            var stream = streamS.get();
            mcmeta.transferTo(stream);
            mcmeta.close(); // TODO take a proper look at where things are closed
            stream.close();
        }
    }
}
