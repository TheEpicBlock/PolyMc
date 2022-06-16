package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.JSoundEvent;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@ApiStatus.Internal
public class JSoundEventRegistryImpl implements JSoundEventRegistry {
    private static final Type TYPE = new TypeToken<Map<String,JSoundEventImpl>>() {}.getType();
    private Map<String, JSoundEvent> jsonRepresentation;

    public JSoundEventRegistryImpl() {
        this.jsonRepresentation = new TreeMap<>();
    }

    public JSoundEventRegistryImpl(Map<String,JSoundEvent> jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    @ApiStatus.Internal
    public static JSoundEventRegistryImpl of(InputStream inputStream, @Nullable String name) {
        try (var jsonReader = new JsonReader(new InputStreamReader(inputStream))) {
            jsonReader.setLenient(true);

            return new JSoundEventRegistryImpl(Util.GSON.fromJson(jsonReader, TYPE));
        } catch (JsonSyntaxException | IOException e) {
            throw new ResourceGenerationException("Error reading sound event registry for "+name, e);
        }
    }

    @Override
    public Map<String,JSoundEvent> getMap() {
        return jsonRepresentation;
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        try (var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            gson.toJson(jsonRepresentation, writer);
        }
    }
}
