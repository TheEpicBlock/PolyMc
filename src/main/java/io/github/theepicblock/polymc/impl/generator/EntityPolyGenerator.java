package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.entity.MissingEntityPoly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

/**
 * Class to automatically generate {@link EntityPoly}s for {@link EntityType}s
 */
public class EntityPolyGenerator {
    /**
     * Automatically generates all {@link EntityPoly}s that are missing in the specified builder
     * @param builder builder to add the {@link EntityPoly}s to
     */
    public static void generateMissing(PolyRegistry builder) {
        for (EntityType<?> entity : getEntityRegistry()) {
            if (builder.hasEntityPoly(entity)) continue;
            Identifier id = getEntityRegistry().getId(entity);
            if (!Util.isVanilla(id)) {
                //this is a modded block and should have a Poly
                addEntityToBuilder(entity, builder);
            }
        }
    }

    /**
     * Generates the most suitable {@link EntityPoly} for a given {@link EntityType}
     */
    public static <T extends Entity> EntityPoly<T> generatePoly(EntityType<T> gui, PolyRegistry builder) {
        return new MissingEntityPoly<>();
    }

    /**
     * Generates the most suitable {@link EntityPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(EntityType, PolyRegistry)
     */
    private static <T extends Entity> void addEntityToBuilder(EntityType<T> entityType, PolyRegistry builder) {
        builder.registerEntityPoly(entityType, generatePoly(entityType, builder));
    }

    /**
     * @return the minecraft entity registry
     */
    private static DefaultedRegistry<EntityType<?>> getEntityRegistry() {
        return Registry.ENTITY_TYPE;
    }
}
