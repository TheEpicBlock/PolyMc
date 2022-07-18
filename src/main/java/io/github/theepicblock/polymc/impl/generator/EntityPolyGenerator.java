package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import io.github.theepicblock.polymc.impl.poly.entity.DefaultedEntityPoly;
import io.github.theepicblock.polymc.impl.poly.entity.MissingEntityPoly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

/**
 * Class to automatically generate {@link EntityPoly}s for {@link EntityType}s
 */
public class EntityPolyGenerator {
    /**
     * Generates the most suitable {@link EntityPoly} for a given {@link EntityType}
     */
    public static <T extends Entity> EntityPoly<T> generatePoly(EntityType<T> entityType, PolyRegistry builder) {
        // Get the class of the entity
        var baseClass = InternalEntityHelpers.getEntityClass(entityType);

        // Iterate over all vanilla entities to see if any are assignable
        var possible = new ArrayList<EntityType<?>>();
        for (var possibleType : Registry.ENTITY_TYPE) {
            var id = Registry.ENTITY_TYPE.getId(possibleType);
            if (Util.isVanilla(id)) {
                Class<?> entityClass = InternalEntityHelpers.getEntityClass(possibleType);

                while (entityClass != LivingEntity.class && entityClass != Entity.class) {
                    if (entityClass.isAssignableFrom(baseClass)) {
                        possible.add(possibleType);
                        break;
                    } else {
                        entityClass = entityClass.getSuperclass();
                    }
                }
            }
        }

        // Sort the list of entities that match by the highest type
        // For example, if both ChestBoatEntity and BoatEntity matched, the boat will be first in the list
        possible.sort((a, b) -> {
            var classA = InternalEntityHelpers.getEntityClass(a);
            var classB = InternalEntityHelpers.getEntityClass(b);
            if (classA == classB) return 0;
            if (classA.isAssignableFrom(classB)) {
                // A is a super type of B, sort it higher
                return 1;
            } else {
                // B is a super type of A
                return -1;
            }
        });

        if (possible.size() > 0) {
            return new DefaultedEntityPoly<>(possible.get(0));
        }

        if (LivingEntity.class.isAssignableFrom(baseClass)) {
            // This is a type of living entity
            return new DefaultedEntityPoly<>(EntityType.ARMOR_STAND);
        }

        if (ProjectileEntity.class.isAssignableFrom(baseClass)) {
            // This is some kind of projectile
            return new DefaultedEntityPoly<>(EntityType.ARROW);
        }

        return new MissingEntityPoly<>();
    }

    /**
     * Generates the most suitable {@link EntityPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(EntityType, PolyRegistry)
     */
    public static <T extends Entity> void addEntityToBuilder(EntityType<T> entityType, PolyRegistry builder) {
        builder.registerEntityPoly(entityType, generatePoly(entityType, builder));
    }
}
