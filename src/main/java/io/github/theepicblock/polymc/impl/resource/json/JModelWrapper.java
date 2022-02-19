package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.*;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import io.github.theepicblock.polymc.impl.resource.ResourceSaveException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class JModelWrapper implements JModel {
    private InputStream inputStreamRepresentation;
    private JModelImpl jsonRepresentation;
    private @Nullable String name;

    public JModelWrapper() {
        this.jsonRepresentation = new JModelImpl();
    }

    public JModelWrapper(JModelImpl jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    public JModelWrapper(InputStream inputStream) {
        if (inputStream == null) {
            throw new NullPointerException();
        }
        this.inputStreamRepresentation = inputStream;
    }

    public JModelWrapper(InputStream inputStream, @Nullable String name) {
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
                this.jsonRepresentation = Util.GSON.fromJson(jsonReader, JModelImpl.class);
            } catch (JsonSyntaxException e) {
                throw new ResourceGenerationException("Error reading model for "+name, e);
            }
        }
    }

    @Override
    public String getParent() {
        assertJson();
        return jsonRepresentation.parent;
    }

    @Override
    public void setParent(String v) {
        assertJson();
        jsonRepresentation.parent = v;
    }

    @Override
    public JGuiLight getGuiLight() {
        assertJson();
        return jsonRepresentation.gui_light;
    }

    @Override
    public void setGuiLight(JGuiLight v) {
        assertJson();
        jsonRepresentation.gui_light = v;
    }

    @Override
    public @NotNull Map<String,String> getTextures() {
        assertJson();
        if (jsonRepresentation.textures == null) {
            jsonRepresentation.textures = new HashMap<>();
        }
        return jsonRepresentation.textures;
    }

    @Override
    public @NotNull List<JElement> getElements() {
        assertJson();
        if (jsonRepresentation.elements == null) {
            jsonRepresentation.elements = new ArrayList<>();
        }
        return jsonRepresentation.elements;
    }

    @Override
    public JModelDisplay getDisplay(JModelDisplayType position) {
        assertJson();
        if (jsonRepresentation.display == null) {
            return null;
        }
        return jsonRepresentation.display.get(position);
    }

    @Override
    public void setDisplay(JModelDisplayType position, JModelDisplay display) {
        assertJson();
        if (jsonRepresentation.display == null) {
            jsonRepresentation.display = new HashMap<>();
        }
        jsonRepresentation.display.put(position, display);
    }

    @Override
    public @NotNull List<JModelOverride> getOverridesReadOnly() {
        assertJson();
        return jsonRepresentation.overrides == null ? Collections.emptyList() : Collections.unmodifiableList(jsonRepresentation.overrides);
    }

    @Override
    public @NotNull List<JModelOverride> getOverrides() {
        assertJson();
        if (jsonRepresentation.overrides == null) {
            jsonRepresentation.overrides = new ArrayList<>();
        }
        return jsonRepresentation.overrides;
    }

    @Override
    public void write(Path location, Gson gson) throws IOException {
        if (inputStreamRepresentation != null) {
            Files.copy(inputStreamRepresentation, location, StandardCopyOption.REPLACE_EXISTING);
        } else if (jsonRepresentation != null) {
            jsonRepresentation.sortOverrides();
            var writer = new FileWriter(location.toFile());
            gson.toJson(jsonRepresentation, writer);
            writer.close();
        } else {
            if (name != null) {
                throw new ResourceSaveException("Failed to save model "+name+". File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            } else {
                throw new ResourceSaveException("Failed to save model. File is unrepresented. This is usually caused by some earlier error concerning json parsing");
            }
        }
    }
}
