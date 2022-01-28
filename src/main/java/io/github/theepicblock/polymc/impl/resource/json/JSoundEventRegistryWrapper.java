package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.JSoundEvent;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class JSoundEventRegistryWrapper implements JSoundEventRegistry {
    private static final Type TYPE = new TypeToken<Map<String,JSoundEventImpl>>() {}.getType();
    private InputStream inputStreamRepresentation;
    private Map<String, JSoundEvent> jsonRepresentation;

    public JSoundEventRegistryWrapper() {
        this.jsonRepresentation = new HashMap<>();
    }

    public JSoundEventRegistryWrapper(HashMap<String,JSoundEvent> jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    public JSoundEventRegistryWrapper(InputStream inputStream) {
        if (inputStream == null) {
            throw new NullPointerException();
        }
        this.inputStreamRepresentation = inputStream;
    }

    /**
     * Asserts that this has been converted to json
     */
    private void assertJson() {
        if (jsonRepresentation == null) {
            var jsonReader = new JsonReader(new InputStreamReader(inputStreamRepresentation));
            jsonReader.setLenient(true);

            this.inputStreamRepresentation = null;
            this.jsonRepresentation = Util.GSON.fromJson(jsonReader, TYPE);
        }
    }

    @Override
    public Map<String,JSoundEvent> getMap() {
        assertJson();
        return jsonRepresentation;
    }

    @Override
    public void write(Path location, Gson gson) throws IOException {
        if (inputStreamRepresentation != null) {
            Files.copy(inputStreamRepresentation, location, StandardCopyOption.REPLACE_EXISTING);
        } else {
            var writer = new FileWriter(location.toFile());
            gson.toJson(jsonRepresentation, writer);
            writer.close();
        }
    }
}
