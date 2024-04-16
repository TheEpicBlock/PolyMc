package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateMultipart;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateVariant;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourceGenerationException;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;

@ApiStatus.Internal
public class JBlockStateImpl implements JBlockState {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    @SerializedName(value = "credit", alternate = "__comment")
    private String credit;

    public final Map<String, JsonElement> variants = new TreeMap<>();
    public final ArrayList<JsonElement> multipart = new ArrayList<>();

    public JBlockStateImpl() {
    }

    @ApiStatus.Internal
    public static JBlockStateImpl of(InputStream inputStream, @Nullable String name) {
        try (var jsonReader = new JsonReader(new InputStreamReader(inputStream));) {
            jsonReader.setLenient(true);

            return Util.GSON.fromJson(jsonReader, JBlockStateImpl.class);
        } catch (JsonSyntaxException | IOException e) {
            throw new ResourceGenerationException("Error reading block state definition for "+name, e);
        }
    }

    @Override
    @Nullable
    public String getMultipartVariantId(BlockState state) {

        StringBuilder result = new StringBuilder();

        // Iterate over all the properties this state has
        for (Property<?> property : state.getProperties()) {
            String key = property.getName();
            String value = state.get(property).toString();

            if (result.isEmpty()) {
                result = new StringBuilder(key + "=" + value);
            } else {
                result.append(",").append(key).append("=").append(value);
            }
        }

        return result.toString();
    }

    /**
     * Try to get all the multipart variants that match the given blockstate.
     * If none are found, return null.
     */
    @Override
    @Nullable
    public JBlockStateVariant[] getMultipartVariantsBestMatching(BlockState state) {

        if (this.multipart.isEmpty()) {
            return null;
        }

        List<JBlockStateVariant> matching_multiparts = new ArrayList<>();

        // If no variants were found, check multipart
        for (JsonElement entry : this.multipart) {

            JBlockStateMultipart multipart = JBlockStateMultipart.from(entry);

            if (multipart == null) {
                continue;
            }

            if (multipart.matches(state)) {
                JBlockStateVariant apply = multipart.getApply();

                if (apply != null) {
                    matching_multiparts.add(apply);
                }
            }
        }

        if (!matching_multiparts.isEmpty()) {
            return matching_multiparts.toArray(new JBlockStateVariant[0]);
        }

        return null;
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

    @Override
    public void setVariant(String propertyString, JBlockStateVariant[] variants) {
        this.variants.put(propertyString, variantsToJsonElement(variants));
    }

    @Override
    public void setMultipart(String propertyString, JBlockStateVariant[] variants) {
        this.multipart.addAll(JBlockStateMultipart.jsonElementFrom(propertyString, variants));
    }

    @Override
    public JBlockStateVariant[] getVariants(String variantString) {
        JBlockStateVariant[] variants = getVariantsFromJsonElement(this.variants.get(variantString));

        // If variants is not null or empty, return it
        if (variants != null && variants.length != 0) {
            return variants;
        }

        // Return empty array if no variants were found
        return new JBlockStateVariant[0];
    }

    @Override
    public Set<String> getPropertyStrings() {
        return this.variants.keySet();
    }

    @Override
    public void writeToStream(OutputStream stream, Gson gson) throws IOException {
        Util.writeJsonToStream(stream, gson, this);
    }
}
