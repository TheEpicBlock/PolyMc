package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.gson.*;
import net.minecraft.entity.data.TrackedDataHandler;

import java.lang.reflect.Type;

public class TrackedDataHandlerJsonHandler implements JsonSerializer<TrackedDataHandler<?>>, JsonDeserializer<TrackedDataHandler<?>> {
    @Override
    public TrackedDataHandler<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(TrackedDataHandler<?> trackedDataHandler, Type type, JsonSerializationContext jsonSerializationContext) {
        return JsonNull.INSTANCE;
    }
}
