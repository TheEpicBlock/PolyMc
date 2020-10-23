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
package io.github.theepicblock.polymc.resource;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonBlockState {
    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    public Map<String,JsonElement> variants;
//    public Map<>

    public JsonBlockState() {
        this.variants = new HashMap<>();
    }

    public static Variant[] getVariants(JsonElement o) {
        if (o instanceof JsonObject) {
            JsonObject b = (JsonObject)o;
            Variant var = new Gson().fromJson(b,Variant.class);
            Variant[] ret = new Variant[1];
            ret[0] = var;
            return ret;
        }
        if (o instanceof JsonArray) {
            JsonArray b = (JsonArray)o;
            return new Gson().fromJson(b,Variant[].class);
        }
        return new Variant[0];
    }

    public JsonElement get(BlockState state) {
        //our goal is to find a string which matches the state the best possible
        propertyPairsLoop:
        for (Map.Entry<String,JsonElement> entry : variants.entrySet()) {
            String propertyPairs = entry.getKey(); //propertyPairs will be in the format 'facing=east,half=lower,hinge=left,open=false'
            JsonElement jsonElement = entry.getValue();

            for (String valuePairString : COMMA_SPLITTER.split(propertyPairs)) {
                String[] valuePair = valuePairString.split("=", 2); //splits `facing=east` into ["facing","east"]
                Property<?> property = state.getBlock().getStateManager().getProperty(valuePair[0]);
                if (property == null) continue propertyPairsLoop; //nope, check the next propertyPairs

                Optional<?> parsedValue = property.parse(valuePair[1]);
                if(!parsedValue.isPresent()) continue propertyPairsLoop; //nope, check the next propertyPairs
                if (!(parsedValue.get() == state.get(property))) {
                    continue propertyPairsLoop;
                }
            }
            return jsonElement;
        }
        return null;
    }

    public static class Variant {
        public String model;
        public int x;
        public int y;
        public boolean uvlock;
    }
}
