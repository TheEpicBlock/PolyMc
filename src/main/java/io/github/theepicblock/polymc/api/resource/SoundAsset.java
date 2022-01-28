package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SoundAsset implements PolyMcAsset {
    private final InputStream inner;

    public SoundAsset(InputStream inner) {
        this.inner = inner;
    }

    @Override
    public void write(Path location, Gson gson) throws IOException {
        Files.copy(inner, location, StandardCopyOption.REPLACE_EXISTING);
    }
}
