package io.github.theepicblock.polymc.impl.generator.asm;

import com.google.gson.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class EntityTypeJsonSerializer<T extends Entity> implements JsonSerializer<EntityType<T>>, JsonDeserializer<EntityType<T>> {
    @SuppressWarnings("unchecked")
    @Override
    public EntityType<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return (EntityType<T>)Registries.ENTITY_TYPE.get((Identifier)jsonDeserializationContext.deserialize(jsonElement, Identifier.class));
    }

    @Override
    public JsonElement serialize(EntityType<T> type, Type type2, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(Registries.ENTITY_TYPE.getId(type));
    }
}
