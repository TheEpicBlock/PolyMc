package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import io.github.theepicblock.polymc.impl.poly.entity.DefaultedEntityPoly;
import io.github.theepicblock.polymc.impl.poly.entity.MissingEntityPoly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class to automatically generate {@link EntityPoly}s for {@link EntityType}s
 */
public class EntityPolyGenerator {
    /**
     * Generates the most suitable {@link EntityPoly} for a given {@link EntityType}
     */
    public static <T extends Entity> EntityPoly<T> generatePoly(EntityType<T> entityType, PolyRegistry builder) {
        var possible = new ArrayList<EntityType<?>>();
        var baseClass = InternalEntityHelpers.getEntityClass(entityType);
        var allSubclasses = new ArrayList<Class<?>>();

        Class<?> tempClass = baseClass;
        while (tempClass != Entity.class) {
            allSubclasses.add(tempClass);
            tempClass = tempClass.getSuperclass();
        }


        for (var possibleType : Registry.ENTITY_TYPE) {
            if (Registry.ENTITY_TYPE.getId(possibleType).getNamespace().equals("minecraft")) {
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


        possible.sort(Comparator.comparingInt(x -> {
            Class<?> clazz = baseClass;
            Class<?> clazzTarget = InternalEntityHelpers.getEntityClass(x);

            int a = 0;
            while (clazz != Object.class) {
                if (clazz.isAssignableFrom(clazzTarget)) {
                    break;
                }

                clazz = clazz.getSuperclass();
                a++;
            }
            return a;
        }));

        return possible.size() > 0
                ? new DefaultedEntityPoly<>(possible.get(0))
                : LivingEntity.class.isAssignableFrom(baseClass)
                ? new DefaultedEntityPoly<>(EntityType.ARMOR_STAND) : new MissingEntityPoly<>();
    }

    /**
     * Generates the most suitable {@link EntityPoly} and directly adds it to the {@link PolyRegistry}
     * @see #generatePoly(EntityType, PolyRegistry)
     */
    public static <T extends Entity> void addEntityToBuilder(EntityType<T> entityType, PolyRegistry builder) {
        builder.registerEntityPoly(entityType, generatePoly(entityType, builder));
    }
}
