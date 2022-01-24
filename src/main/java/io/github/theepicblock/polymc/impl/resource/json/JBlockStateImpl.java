package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class JBlockStateImpl {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    private String credit;

    public final Map<String, JsonElement> variants = new HashMap<>();
}
