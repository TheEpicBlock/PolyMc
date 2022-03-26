package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateVariant;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import io.github.theepicblock.polymc.impl.resource.ResourceSaveException;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class JBlockStateWrapper implements JBlockState {
    private InputStream inputStreamRepresentation;
    private JBlockStateImpl jsonRepresentation;
    private @Nullable String name;

    public JBlockStateWrapper() {
        this.jsonRepresentation = new JBlockStateImpl();
    }

    public JBlockStateWrapper(JBlockStateImpl jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    public JBlockStateWrapper(InputStream inputStream) {
        if (inputStream == null) {
            throw new NullPointerException();
        }
        this.inputStreamRepresentation = inputStream;
    }

    public JBlockStateWrapper(InputStream inputStream, @Nullable String name) {
        this(inputStream);
        this.name = name;
    }

    /**
     * The json can contain either a single variant object or a list of them. This normalizes them to a list.
     */
    private static JBlockStateVariant[] getVariantsFromJsonElement(JsonElement input) {
        if (input instanceof JsonObject jsonObject) {
            var variant = new Gson().fromJson(jsonObject, JBlockStateVariant.class);
            var returnArray = new JBlockStateVariant[1];
            returnArray[0] = variant;
            return returnArray;
        }
        if (input instanceof JsonArray jsonArray) {
            return new Gson().fromJson(jsonArray, JBlockStateVariant[].class);
        }
        return new JBlockStateVariant[0];
    }

    private static JsonElement variantsToJsonElement(JBlockStateVariant[] variants) {
        if (variants.length == 0) {
            return null;
        } else if (variants.length == 1) {
            new Gson().toJsonTree(variants[0]);
        }
        return new Gson().toJsonTree(variants);
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
                this.jsonRepresentation = Util.GSON.fromJson(jsonReader, JBlockStateImpl.class);
            } catch (JsonSyntaxException e) {
                throw new ResourceGenerationException("Error reading block state definition for "+name, e);
            }
        }
    }

    @Override
    public void setVariant(String propertyString, JBlockStateVariant[] variants) {
        assertJson();
        this.jsonRepresentation.variants.put(propertyString, variantsToJsonElement(variants));
    }

    @Override
    public JBlockStateVariant[] getVariants(String variantString) {
        assertJson();
        return getVariantsFromJsonElement(this.jsonRepresentation.variants.get(variantString));
    }

    @Override
    public Set<String> getPropertyStrings() {
        assertJson();
        return this.jsonRepresentation.variants.keySet();
    }

    @Override
    public void write(Path location, Gson gson) throws IOException {
        if (inputStreamRepresentation != null) {
            Files.copy(inputStreamRepresentation, location, StandardCopyOption.REPLACE_EXISTING);
        } else if (jsonRepresentation != null) {
            if (!jsonRepresentation.variants.isEmpty()) { // TODO check for multipart
                var writer = new FileWriter(location.toFile());
                gson.toJson(jsonRepresentation, writer);
                writer.close();
            }
        } else {
            if (name != null) {
                throw new ResourceSaveException("Failed to save block state definition "+name+". File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            } else {
                throw new ResourceSaveException("Failed to save block state definition. File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            }
        }
    }
}
