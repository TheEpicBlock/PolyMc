package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JModelOverride that = (JModelOverride)o;
        return Objects.equals(predicates, that.predicates) && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicates, model);
    }
}
