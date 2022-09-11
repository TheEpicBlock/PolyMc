package io.github.theepicblock.polymc.impl.generator;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import io.github.theepicblock.polymc.impl.poly.entity.DefaultedEntityPoly;
import io.github.theepicblock.polymc.impl.poly.entity.FlyingItemEntityPoly;
import io.github.theepicblock.polymc.impl.poly.entity.MissingEntityPoly;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;

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

        if (baseClass == null) return new MissingEntityPoly<>();

        // Iterate over all vanilla entities to see if any are assignable
        var possible = new ArrayList<EntityType<?>>();
        for (var possibleType : Registry.ENTITY_TYPE) {
            var id = Registry.ENTITY_TYPE.getId(possibleType);
            if (Util.isVanilla(id)) {
                Class<?> vanillaEntityClass = InternalEntityHelpers.getEntityClass(possibleType);

                if (vanillaEntityClass != null && vanillaEntityClass.isAssignableFrom(baseClass)) {
                    possible.add(possibleType);
                }
            }
        }

        // Players are blacklisted, we shouldn't spawn any players.
        possible.removeIf(clazz -> clazz == EntityType.PLAYER);

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

        if (FlyingItemEntity.class.isAssignableFrom(baseClass)) {
            return new FlyingItemEntityPoly();
        }

        if (GolemEntity.class.isAssignableFrom(baseClass)) {
            if (entityType.getWidth() > 1) {
                return new DefaultedEntityPoly<>(EntityType.IRON_GOLEM);
            } else {
                return new DefaultedEntityPoly<>(EntityType.SNOW_GOLEM);
            }
        }

        var otherCommonClasses = new HashMap<Class<?>, EntityType<?>>();
        otherCommonClasses.put(AbstractDonkeyEntity.class, EntityType.DONKEY);
        otherCommonClasses.put(AbstractHorseEntity.class, EntityType.HORSE);
        otherCommonClasses.put(AbstractPiglinEntity.class, EntityType.PIGLIN);
        otherCommonClasses.put(AbstractSkeletonEntity.class, EntityType.SKELETON);
        otherCommonClasses.put(AbstractMinecartEntity.class, EntityType.MINECART);
        otherCommonClasses.put(ProjectileEntity.class, EntityType.ARROW);
        otherCommonClasses.put(FishEntity.class, EntityType.COD);
        otherCommonClasses.put(FlyingEntity.class, EntityType.PARROT);
        otherCommonClasses.put(Flutterer.class, EntityType.PARROT);

        for (var clazz : otherCommonClasses.keySet()) {
            if (clazz.isAssignableFrom(baseClass)) {
                return new DefaultedEntityPoly<>(otherCommonClasses.get(clazz));
            }
        }

        if (LivingEntity.class.isAssignableFrom(baseClass)) {
            if (entityType.getHeight() > 1.5) {
                if (Monster.class.isAssignableFrom(baseClass)) {
                    return new DefaultedEntityPoly<>(EntityType.ZOMBIE);
                } else {
                    return new DefaultedEntityPoly<>(EntityType.ARMOR_STAND);
                }
            } else if (entityType.getHeight() > 0.5) {
                return new DefaultedEntityPoly<>(EntityType.PIG);
            } else {
                return new DefaultedEntityPoly<>(EntityType.SILVERFISH);
            }
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
