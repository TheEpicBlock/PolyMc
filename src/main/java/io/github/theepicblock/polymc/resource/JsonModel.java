package io.github.theepicblock.polymc.resource;

import java.util.ArrayList;
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

    public static class Override {
        public Map<String,Integer> predicate;
        public String model;
    }
}
