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
package io.github.theepicblock.polymc.api.resource.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class JSoundsRegistry {
	public static final Type TYPE = new TypeToken<Map<String,SoundEventEntry>>() {}.getType();

    public static String getNamespace(JsonElement soundEventEntry) {
        if (soundEventEntry.isJsonPrimitive()) {
            return soundEventEntry.getAsString();
        } else if (soundEventEntry.isJsonObject()) {
            return new Gson().fromJson(soundEventEntry, Sound.class).name;
        }
        throw new JsonParseException("Sounds array contains an object that's neither a string nor a Sound object");
    }

    public static class SoundEventEntry {
        public String category;
        public JsonElement[] sounds; //can be either a string or a Sound object
    }

    public static class Sound {
        public String name;
        public float volume;
        public float pitch;
        public int weight;
        public boolean stream;
    }
}
