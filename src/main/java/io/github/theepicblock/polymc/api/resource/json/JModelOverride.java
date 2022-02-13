package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public class JModelOverride {
    @SerializedName("predicate")
    private final Map<String, Float> predicates;
    private final String model;

    public JModelOverride(Map<String,Float> predicates, String model) {
        this.predicates = predicates;
        this.model = model;
    }

    public static JModelOverride ofCMD(int cmdValue, String model) {
        return new JModelOverride(Collections.singletonMap("custom_model_data", (float)cmdValue), model);
    }

    public Map<String, Float> predicates() {
        return predicates;
    }

    public String model() {
        return model;
    }
}
