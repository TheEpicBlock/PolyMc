package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public class JBlockStateMultipart {

    protected JBlockStateVariant apply;
    protected JsonElement when;
    protected JMultipartCondition parsedConditions = null;

    /**
     * Create a new JBlockStateMultipart from a JsonElement
     */
    public static JBlockStateMultipart from(JsonElement entry) {

        if (entry == null || !entry.isJsonObject()) {
            return null;
        }

        JsonObject object = entry.getAsJsonObject();

        JBlockStateMultipart result = new JBlockStateMultipart();
        result.apply = new Gson().fromJson(object.get("apply"), JBlockStateVariant.class);
        result.parsedConditions = new JMultipartCondition(object.get("when"));

        return result;
    }

    /**
     * Create a new JBlockStateMultipart from a property string and a list of variants
     */
    public static List<JsonElement> jsonElementFrom(String propertyString, JBlockStateVariant[] variants) {

        List<JsonElement> result = new ArrayList<>();

        if (variants == null) {
            return result;
        }

        for (var variant : variants) {
            JsonObject object = new JsonObject();
            object.add("apply", new Gson().toJsonTree(variant));

            JsonObject when = new JsonObject();

            for (var property : Util.splitBlockStateString(propertyString)) {
                // Split "facing=east" into "facing" and "east"
                var pair = property.split("=", 2);
                when.addProperty(pair[0], pair[1]);
            }

            object.add("when", when);
            result.add(object);
        }

        return result;
    }

    public JBlockStateVariant getApply() {
        return apply;
    }

    /**
     * Get the parsed conditions for this multipart entry
     */
    public JMultipartCondition getConditions() {
        if (this.parsedConditions == null) {
            this.parsedConditions = new JMultipartCondition(this.when);
        }

        return this.parsedConditions;
    }

    /**
     * See if this multipart entry matches the given modded BlockState
     */
    public boolean matches(BlockState state) {

        if (this.when == null && this.parsedConditions == null) {
            return true;
        }

        return this.getConditions().matches(state);
    }
}
