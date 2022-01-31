package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.JSoundEvent;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import io.github.theepicblock.polymc.impl.resource.ResourceSaveException;
import org.jetbrains.annotations.Nullable;

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
    private @Nullable String name;

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

    public JSoundEventRegistryWrapper(InputStream inputStream, @Nullable String name) {
        this(inputStream);
        this.name = name;
    }

    /**
     * Asserts that this has been converted to json
     */
    private void assertJson() {
        if (jsonRepresentation == null) {
            try {
                var jsonReader = new JsonReader(new InputStreamReader(inputStreamRepresentation));
                jsonReader.setLenient(true);

                this.inputStreamRepresentation = null;
                this.jsonRepresentation = Util.GSON.fromJson(jsonReader, TYPE);
            } catch (JsonSyntaxException e) {
                throw new ResourceGenerationException("Error reading sound event registry for "+name, e);
            }
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
        } else if (jsonRepresentation != null) {
            var writer = new FileWriter(location.toFile());
            gson.toJson(jsonRepresentation, writer);
            writer.close();
        } else {
            if (name != null) {
                throw new ResourceSaveException("Failed to save sound event registry "+name+". File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            } else {
                throw new ResourceSaveException("Failed to save sound event registry. File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            }
        }
    }
}
