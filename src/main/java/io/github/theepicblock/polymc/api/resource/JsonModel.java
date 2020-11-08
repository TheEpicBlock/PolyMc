/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java representation of a blockstate json file.
 * Note that it isn't completely accurate.
 */
public class JsonModel {
    public String parent;
    public String gui_light;
    public Map<String,String> textures;
    public Map<String,DisplayEntry> display;
    public List<Override> overrides;

    public void addOverride(Override e) {
        if (overrides == null) {
            overrides = new ArrayList<>();
        }
        overrides.add(e);
    }

    /**
     * Please use instead of {@link Gson#toJson(Object)} as it makes sure the {@link #overrides} list is properly sorted before converting.
     */
    public String toJson(Gson gson) {
        //Ensure that the overrides are in the correct order before converting
        overrides.sort((o1, o2) -> {
            if (o1.predicate.size() > 0 && o2.predicate.size() > 0) {
                double i1 = o1.predicate.values().iterator().next();
                double i2 = o2.predicate.values().iterator().next();
                return (int)(i1 - i2);
            }
            return 0;
        });

        return gson.toJson(this);
    }

    public static class Override {
        public Map<String,Double> predicate;
        public String model;
    }

    public static class DisplayEntry {
        public double[] rotation;
        public double[] translation;
        public double[] scale;
    }
}
