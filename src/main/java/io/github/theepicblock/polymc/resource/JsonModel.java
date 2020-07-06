package io.github.theepicblock.polymc.resource;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonModel {
    public String parent;
    public Map<String,String> textures;
    public List<Override> overrides;

    public void addOverride(Override e) {
        if (overrides == null) {
            overrides = new ArrayList<>();
        }
        overrides.add(e);
    }

    public String toJson(Gson gson) {
        //Ensure that the overrides are in the correct order before converting
        Collections.sort(overrides,(o1,o2) -> {
            if (o1.predicate.size() > 0 && o2.predicate.size() > 0) {
                int i1 = o1.predicate.values().iterator().next();
                int i2 = o2.predicate.values().iterator().next();
                return i1 - i2;
            }
            return 0;
        });

        return gson.toJson(this);
    }

    public static class Override {
        public Map<String,Integer> predicate;
        public String model;
    }
}
