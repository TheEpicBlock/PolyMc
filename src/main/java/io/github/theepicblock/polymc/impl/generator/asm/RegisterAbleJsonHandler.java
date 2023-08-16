package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.gson.*;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class RegisterAbleJsonHandler<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private final Registry<T> registry;

    public RegisterAbleJsonHandler(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return registry.get((Identifier)jsonDeserializationContext.deserialize(jsonElement, Identifier.class));
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(registry.getId(t));
    }
}
