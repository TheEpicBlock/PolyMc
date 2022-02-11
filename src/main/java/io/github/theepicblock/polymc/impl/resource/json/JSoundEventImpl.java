package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.theepicblock.polymc.api.resource.json.JSoundEvent;
import io.github.theepicblock.polymc.api.resource.json.JSoundReference;
import io.github.theepicblock.polymc.impl.Util;

import java.util.List;
import java.util.stream.Collectors;

public class JSoundEventImpl implements JSoundEvent {
    private boolean replace;
    private String subtitle;
    private List<JsonElement> sounds;

    @Override
    public boolean getReplace() {
        return replace;
    }

    @Override
    public void setReplace(boolean v) {
        replace = v;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public void setSubtitle(String v) {
        subtitle = v;
    }

    @Override
    public List<JSoundReference> getSounds() {
        return sounds.stream().map(jsonElement -> {
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                var string = jsonElement.getAsString();
                var ref = new JSoundReference();
                ref.name = string;
                return ref;
            } else {
                return Util.GSON.fromJson(jsonElement, JSoundReference.class);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void setSounds(List<JSoundReference> newSounds) {
        sounds = newSounds.stream().map(reference -> {
            if (reference.isNameOnly()) {
                return new JsonPrimitive(reference.name);
            } else {
                return Util.GSON.toJsonTree(reference);
            }
        }).collect(Collectors.toList());
    }
}
