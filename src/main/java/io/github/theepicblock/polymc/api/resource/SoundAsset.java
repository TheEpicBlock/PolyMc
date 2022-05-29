package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.impl.resource.PolyMcAssetBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

public class SoundAsset extends PolyMcAssetBase implements PolyMcAsset {
    private final Supplier<InputStream> inner;

    public SoundAsset(Supplier<InputStream> inner) {
        this.inner = inner;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        try (var iStream = inner.get()) {
            iStream.transferTo(stream);
        }
    }
}
