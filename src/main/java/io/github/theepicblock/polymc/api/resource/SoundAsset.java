package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.impl.resource.PolyMcAssetBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SoundAsset extends PolyMcAssetBase implements PolyMcAsset {
    private final InputStream inner;

    public SoundAsset(InputStream inner) {
        this.inner = inner;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        inner.transferTo(stream);
        inner.close(); // TODO take a proper look at where things are closed
    }
}
