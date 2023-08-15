package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalTypeAdapter<T> implements JsonSerializer<Optional<T>>, JsonDeserializer<Optional<T>> {
    @Override
    public JsonElement serialize(Optional<T> t, Type type, JsonSerializationContext jsonSerializationContext) {
        var o = new JsonObject();
        o.addProperty("type", t.map(value -> value.getClass().getName()).orElse("null"));
        if (t.isPresent()) {
            o.add("data", jsonSerializationContext.serialize(o));
        }
        return o;
    }

    @Override
    public Optional<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var innerT = jsonElement.getAsJsonObject().get("type").getAsString();
        if (innerT.equals("null")) {
            return Optional.empty();
        } else {
            try {
                return jsonDeserializationContext.deserialize(jsonElement.getAsJsonObject().get("data"), Class.forName(innerT));
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
