package io.github.theepicblock.polymc.impl.misc;

import com.mojang.authlib.GameProfile;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Copy of Polymer's InternalEntityHelpers class
@ApiStatus.Internal
@SuppressWarnings({"unused", "unchecked"})
public class InternalEntityHelpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final PigEntity PIG = new PigEntity(EntityType.PIG, FakeWorld.INSTANCE);

    public static List<DataTracker.Entry<?>> getExampleTrackedDataOfEntityType(EntityType<?> type) {
        return getEntity(type).getDataTracker().getAllEntries();
    }

    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return (Class<T>) getEntity(type).getClass();
    }

    public static Entity getEntity(EntityType<?> type) {
        var entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            try {
                entity = type.create(FakeWorld.INSTANCE);
            } catch (Throwable e) {
                var id = Registry.ENTITY_TYPE.getId(type);
                PolyMc.LOGGER.warn(String.format(
                        "Couldn't create template entity of %s (%s)... Defaulting to empty. %s",
                        id,
                        type.getBaseClass().toString(),
                        Util.isVanilla(id) ? "This might cause problems!" : "Don't worry, this shouldn't cause problems!"
                ));

                if (Util.isVanilla(id) || FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    e.printStackTrace();
                }
                // Default for when we couldn't create the entity
                entity = new Entity(EntityType.PIG, null) {
                    @Override
                    protected void initDataTracker() {

                    }

                    @Override
                    protected void readCustomDataFromNbt(NbtCompound nbt) {

                    }

                    @Override
                    protected void writeCustomDataToNbt(NbtCompound nbt) {

                    }

                    @Override
                    public Packet<?> createSpawnPacket() {
                        return null;
                    }
                };
            }
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity;
    }


    static {
        EXAMPLE_ENTITIES.put(EntityType.PLAYER, new PlayerEntity(FakeWorld.INSTANCE, BlockPos.ORIGIN, 0, new GameProfile(net.minecraft.util.Util.NIL_UUID, "TinyPotato"), new PlayerPublicKey(null)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        });

        EXAMPLE_ENTITIES.put(EntityType.PIG, PIG);
    }
}
