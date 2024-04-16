package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.*;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public class JMultipartCondition {

    /**
     * Were any conditions given?
     */
    protected boolean has_conditions = false;

    /**
     * Optionally parsed AND group
     */
    protected List<JMultipartCondition> AND = null;

    /**
     * Optionally parsed OR group
     */
    protected List<JMultipartCondition> OR = null;

    /**
     * All the property conditions and their allowed values
     * (These values are delimited by a pipe in the original JSON string)
     */
    protected Map<String, Set<String>> properties = null;

    /**
     * Initialize a new instance of JMultipartCondition from a JsonElement
     */
    public JMultipartCondition(JsonElement data) {

        // If the data is not an object, nothing needs to happen
        // and this condition will always match.
        if (!(data instanceof JsonObject jsonObject)) {
            return;
        }

        // Iterate over the object
        for (var entry : jsonObject.entrySet()) {
            this.has_conditions = true;
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            boolean is_and = key.equals("AND");
            boolean is_or = key.equals("OR");

            // If the key is "AND" or "OR", it should be an array
            if (is_and || is_or) {
                if (value instanceof JsonArray jsonArray) {
                    int size = jsonArray.size();

                    if (is_and && this.AND == null) {
                        this.AND = new ArrayList<>(size);
                    } else if (is_or && this.OR == null) {
                        this.OR = new ArrayList<>(size);
                    }

                    for (JsonElement element : jsonArray) {
                        if (element instanceof JsonObject) {
                            if (is_and) {
                                this.AND.add(new JMultipartCondition(element));
                            } else {
                                this.OR.add(new JMultipartCondition(element));
                            }
                        }
                    }
                }
            } else {

                if (this.properties == null) {
                    this.properties = new TreeMap<>();
                }

                Set<String> allowed_values = this.properties.computeIfAbsent(key, k -> new TreeSet<>());

                String value_string = value.getAsString();

                // Split the values by pipe char
                allowed_values.addAll(Arrays.asList(value_string.split("\\|")));
            }
        }
    }

    /**
     * See if the given (modded) BlockState matches this condition
     */
    public boolean matches(BlockState state) {

        // If there are no conditions, it should always match
        if (!this.has_conditions) {
            return true;
        }

        // If there is an AND group,
        // make sure all entries match
        if (this.AND != null && !this.AND.isEmpty()) {
            for (JMultipartCondition condition : this.AND) {
                if (!condition.matches(state)) {
                    return false;
                }
            }

            return true;
        }

        // If there is an OR group,
        // make sure at least 1 entry matches
        if (this.OR != null && !this.OR.isEmpty()) {
            boolean orMatch = false;
            for (JMultipartCondition condition : this.OR) {
                if (condition.matches(state)) {
                    orMatch = true;
                    break;
                }
            }

            return orMatch;
        }

        if (this.properties == null) {
            return true;
        }

        // Iterate over the key & value pairs
        for (Map.Entry<String, Set<String>> entry : this.properties.entrySet()) {
            String key = entry.getKey();
            Set<String> allowed_values = entry.getValue();

            // Get the property
            Property<?> property = state.getBlock().getStateManager().getProperty(key);

            // If the property is null, return false
            if (property == null) {
                return false;
            }

            Comparable<?> value = state.get(property);

            // If the value is null, return false
            if (value == null) {
                return false;
            }

            if (!allowed_values.contains(value.toString())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "JMultipartCondition{" +
                "AND=" + AND +
                ", OR=" + OR +
                ", properties=" + properties +
                '}';
    }
}
