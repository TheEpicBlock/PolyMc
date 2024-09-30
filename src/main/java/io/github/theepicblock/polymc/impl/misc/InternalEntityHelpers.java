package io.github.theepicblock.polymc.impl.misc;

import com.mojang.authlib.GameProfile;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.ConfigManager;
import io.github.theepicblock.polymc.mixins.entity.DataTrackerAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

// Copy of Polymer's InternalEntityHelpers class
@ApiStatus.Internal
@SuppressWarnings({"unused", "unchecked"})
public class InternalEntityHelpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final Map<EntityType<?>, DataTracker.Entry<?>[]> TRACKED_DATA = new IdentityHashMap<>();

    private static PlayerEntity createPlayer() {
        PlayerEntity player = null;
        try {
            player = new PlayerEntity(FakeWorld.INSTANCE_UNSAFE, BlockPos.ORIGIN, 0, new GameProfile(net.minecraft.util.Util.NIL_UUID, "TinyPotato")) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
        } catch (Throwable e) {
            if (ConfigManager.getConfig().logEntityTemplateErrors) {
                PolyMc.LOGGER.error("Failed add player like entity! Trying with alternative method");
                e.printStackTrace();
            }
            try {
                player = new PlayerEntity(FakeWorld.INSTANCE_REGULAR, BlockPos.ORIGIN, 0, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }
                };
            } catch (Throwable e2) {
                if (ConfigManager.getConfig().logEntityTemplateErrors) {
                    PolyMc.LOGGER.error("Failed add player like entity!");
                    e.printStackTrace();
                }
            }
        }
        EXAMPLE_ENTITIES.put(EntityType.PLAYER, player);
        return player;
    };

    public static DataTracker.Entry<?>[] getExampleTrackedDataOfEntityType(EntityType<?> type) {
        var val = TRACKED_DATA.get(type);

        if (val == null) {
            var ent = getEntity(type);
            if (ent != null) {
                var map = ((DataTrackerAccessor) ent.getDataTracker()).getEntries();
                TRACKED_DATA.put(type, map);
                return map;
            }
        }

        return val;
    }

    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return (Class<T>) getEntity(type).getClass();
    }

    public static boolean isLivingEntity(EntityType<?> type) {
        return getEntity(type) instanceof LivingEntity;
    }

    public static boolean isMobEntity(EntityType<?> type) {
        return getEntity(type) instanceof MobEntity;
    }

    public static boolean canPatchTrackedData(ServerPlayerEntity player, Entity entity) {
        return true;
    }

    public static Entity getEntity(EntityType<?> type) {
        Entity entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            if (type == EntityType.PLAYER) {
                return createPlayer();
            }

            try {
                entity = type.create(FakeWorld.INSTANCE_UNSAFE);
            } catch (Throwable e) {
                try {
                    entity = type.create(FakeWorld.INSTANCE_REGULAR);
                } catch (Throwable e2) {
                    var id = Registries.ENTITY_TYPE.getId(type);
                    if (ConfigManager.getConfig().logEntityTemplateErrors) {
                         PolyMc.LOGGER.warn(String.format(
                                "Couldn't create template entity of %s... Defaulting to empty. %s",
                                id,
                                id.getNamespace().equals("minecraft") ? "This might cause problems!" : "Don't worry, this shouldn't cause problems!"
                        ));

                        if (id.getNamespace().equals("minecraft")) {
                             PolyMc.LOGGER.warn("First error:");
                            e.printStackTrace();
                             PolyMc.LOGGER.warn("Second error:");
                            e2.printStackTrace();
                        }
                    }
                    entity = FakeEntity.INSTANCE;
                }

            }
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity;
    }

    public static Entity getFakeEntity() {
        return FakeEntity.INSTANCE;
    }
}
