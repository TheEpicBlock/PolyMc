package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import net.minecraft.resource.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SoundAsset implements PolyMcAsset {
    private final InputSupplier<InputStream> inner;

    public SoundAsset(InputSupplier<InputStream> inner) {
        this.inner = inner;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        try (var iStream = inner.get()) {
            iStream.transferTo(stream);
        }
    }
}
