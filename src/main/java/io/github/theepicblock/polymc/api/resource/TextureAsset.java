package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import net.minecraft.resource.InputSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextureAsset implements PolyMcAsset {
    private final @NotNull InputSupplier<InputStream> texture;
    private final @Nullable InputSupplier<InputStream> mcmeta;

    public TextureAsset(@NotNull InputSupplier<InputStream> inner, @Nullable InputSupplier<InputStream> mcmeta) {
        this.texture = inner;
        this.mcmeta = mcmeta;
    }

    public @NotNull InputStream getTexture() throws IOException {
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
