package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.impl.resource.PolyMcAssetBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

public class TextureAsset extends PolyMcAssetBase implements PolyMcAsset {
    private final @NotNull Supplier<InputStream> texture;
    private final @Nullable Supplier<InputStream> mcmeta;

    public TextureAsset(@NotNull Supplier<InputStream> inner, @Nullable Supplier<InputStream> mcmeta) {
        this.texture = inner;
        this.mcmeta = mcmeta;
    }

    public @NotNull InputStream getTexture() {
        return texture.get();
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        try (var iStream = texture.get()) {
            iStream.transferTo(stream);
        }
    }

    @Override
    public void writeMetaToStream(StreamSupplier streamS, Gson gson) throws IOException {
        if (mcmeta != null) {
            try (var iStream = mcmeta.get()) {
                var oStream = streamS.get();
                iStream.transferTo(oStream);
                oStream.close();
            }
        }
    }
}
