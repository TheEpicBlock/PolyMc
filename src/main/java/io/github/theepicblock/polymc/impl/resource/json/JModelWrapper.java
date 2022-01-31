package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.*;
import io.github.theepicblock.polymc.impl.Util;

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

    /**
     * Asserts that this has been converted to json
     */
    private void assertJson() {
        if (jsonRepresentation == null) {
            var jsonReader = new JsonReader(new InputStreamReader(inputStreamRepresentation));
            jsonReader.setLenient(true);

            this.inputStreamRepresentation = null;
            this.jsonRepresentation = Util.GSON.fromJson(jsonReader, JModelImpl.class);
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
    public Map<String,String> getTextures() {
        assertJson();
        if (jsonRepresentation.textures == null) {
            jsonRepresentation.textures = new HashMap<>();
        }
        return jsonRepresentation.textures;
    }

    @Override
    public List<JElement> getElements() {
        assertJson();
        if (jsonRepresentation.elements == null) {
            jsonRepresentation.elements = new ArrayList<>();
        }
        return jsonRepresentation.elements;
    }

    @Override
    public JModelDisplay getDisplay(JModelDisplayType position) {
        assertJson();
        return jsonRepresentation.display.get(position);
    }

    @Override
    public void setDisplay(JModelDisplayType position, JModelDisplay display) {
        assertJson();
        jsonRepresentation.display.put(position, display);
    }

    @Override
    public List<JModelOverride> getOverridesReadOnly() {
        assertJson();
        return jsonRepresentation.overrides == null ? Collections.emptyList() : Collections.unmodifiableList(jsonRepresentation.overrides);
    }

    @Override
    public List<JModelOverride> getOverrides() {
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
        } else {
            jsonRepresentation.sortOverrides();
            var writer = new FileWriter(location.toFile());
            gson.toJson(jsonRepresentation, writer);
            writer.close();
        }
    }
}
