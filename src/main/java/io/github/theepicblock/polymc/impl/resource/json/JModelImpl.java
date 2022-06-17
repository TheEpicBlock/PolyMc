package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.json.*;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JModelImpl implements JModel {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    @SerializedName(value = "credit", alternate = "__comment")
    private String credit;

    public String parent;
    public JGuiLight gui_light;
    public Map<String, String> textures;
    public List<JElement> elements;
    public Map<JModelDisplayType,JModelDisplay> display;
    public List<JModelOverride> overrides;

    public JModelImpl() {

    }

    @ApiStatus.Internal
    public static JModelImpl of(InputStream inputStream, @Nullable String name) {
        try (var jsonReader = new JsonReader(new InputStreamReader(inputStream))) {
            jsonReader.setLenient(true);

            return Util.GSON.fromJson(jsonReader, JModelImpl.class);
        } catch (JsonSyntaxException | IOException e) {
            throw new ResourceGenerationException("Error reading model for "+name, e);
        }
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public void setParent(String v) {
        parent = v;
    }

    @Override
    public JGuiLight getGuiLight() {
        return this.gui_light;
    }

    @Override
    public void setGuiLight(JGuiLight v) {
        this.gui_light = v;
    }

    @Override
    public @NotNull Map<String,String> getTextures() {
        if (this.textures == null) {
            this.textures = new TreeMap<>();
        }
        return this.textures;
    }

    @Override
    public @NotNull List<JElement> getElements() {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }
        return this.elements;
    }

    @Override
    public JModelDisplay getDisplay(JModelDisplayType position) {
        if (this.display == null) {
            return null;
        }
        return this.display.get(position);
    }

    @Override
    public void setDisplay(JModelDisplayType position, JModelDisplay display) {
        if (this.display == null) {
            this.display = new TreeMap<>();
        }
        this.display.put(position, display);
    }

    @Override
    public @NotNull List<JModelOverride> getOverridesReadOnly() {
        return overrides == null ? Collections.emptyList() : Collections.unmodifiableList(overrides);
    }

    @Override
    public @NotNull List<JModelOverride> getOverrides() {
        if (overrides == null) {
            overrides = new ArrayList<>();
        }
        return overrides;
    }

    /**
     * Ensures that the {@link #overrides} list is properly sorted so that the lowest priority go on top.
     */
    private void sortOverrides() {
        if (overrides == null) return;
        overrides.sort((o1, o2) -> {
            if (o1.predicates().size() > 0 && o2.predicates().size() > 0) {
                double i1 = o1.predicates().values().iterator().next();
                double i2 = o2.predicates().values().iterator().next();
                return (int)(i1 - i2);
            }
            return 0;
        });
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        this.sortOverrides();
        try (var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        }
    }
}
