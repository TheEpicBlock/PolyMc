package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.gson.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Type;

public class RegistryEntryJsonSerializer<T> implements JsonSerializer<RegistryEntry.Reference<T>>, JsonDeserializer<RegistryEntry.Reference<T>> {
    @Override
    public RegistryEntry.Reference<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var key = (RegistryKey<?>)jsonDeserializationContext.deserialize(jsonElement, RegistryKey.class);
        var registry = (Registry<T>)Registries.REGISTRIES.get(key.getRegistry());
        assert registry != null;
        return RegistryEntry.Reference.intrusive(registry.getEntryOwner(), registry.get(key.getValue()));
    }

    @Override
    public JsonElement serialize(RegistryEntry.Reference<T> tReference, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(tReference.registryKey());
    }
}
