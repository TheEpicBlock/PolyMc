package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.mixins.ReferenceAccessor;
import io.github.theepicblock.polymc.mixins.WorldAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Copy of Polymer's FakeWorld class

@ApiStatus.Internal
public final class FakeWorld extends World {
    public static final World INSTANCE;

    public static final World INSTANCE_UNSAFE;
    public static final World INSTANCE_REGULAR;
    static final Scoreboard SCOREBOARD = new Scoreboard();
    static final DynamicRegistryManager REGISTRY_MANAGER = DynamicRegistryManager.EMPTY;
    static final RecipeManager RECIPE_MANAGER = new RecipeManager();
    private static final FeatureSet FEATURES = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
    final ChunkManager chunkManager = new ChunkManager() {
        private LightingProvider lightingProvider = null;

        @Nullable
        @Override
        public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {

        }

        @Override
        public String getDebugString() {
            return "Potato";
        }

        @Override
        public int getLoadedChunkCount() {
            return 0;
        }

        @Override
        public LightingProvider getLightingProvider() {
            if (this.lightingProvider == null) {
                this.lightingProvider = new LightingProvider(new ChunkProvider() {
                    @Nullable
                    @Override
                    public BlockView getChunk(int chunkX, int chunkZ) {
                        return null;
                    }

                    @Override
                    public BlockView getWorld() {
                        return FakeWorld.this;
                    }
                }, false, false);
            }

            return this.lightingProvider;
        }

        @Override
        public BlockView getWorld() {
            return FakeWorld.this;
        }
    };
    private static final EntityLookup<Entity> ENTITY_LOOKUP = new EntityLookup<>() {
        @Nullable
        @Override
        public Entity get(int id) {
            return null;
        }

        @Nullable
        @Override
        public Entity get(UUID uuid) {
            return null;
        }

        @Override
        public Iterable<Entity> iterate() {
            return () -> ObjectIterators.emptyIterator();
        }

        @Override
        public <U extends Entity> void forEach(TypeFilter<Entity, U> filter, LazyIterationConsumer<U> consumer) {

        }

        @Override
        public void forEachIntersects(Box box, Consumer<Entity> action) {

        }

        @Override
        public <U extends Entity> void forEachIntersects(TypeFilter<Entity, U> filter, Box box, LazyIterationConsumer<U> consumer) {

        }

    };
    private static final QueryableTickScheduler<?> FAKE_SCHEDULER = new QueryableTickScheduler<Object>() {
        @Override
        public boolean isTicking(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public void scheduleTick(OrderedTick<Object> orderedTick) {

        }

        @Override
        public boolean isQueued(BlockPos pos, Object type) {
            return false;
        }

        @Override
        public int getTickCount() {
            return 0;
        }
    };

    static {
        World worldUnsafe, worldDefault;

        var dimType = RegistryEntry.Reference.intrusive(new RegistryEntryOwner<>() {}, new DimensionType(OptionalLong.empty(), true, false, false, true, 1.0D, true, false, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, DimensionTypes.OVERWORLD_ID, 0.0F, new DimensionType.MonsterSettings(false, true, UniformIntProvider.create(0, 7), 0)));
        ((ReferenceAccessor) dimType).callSetRegistryKey(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("overworld")));
        try {
            worldUnsafe = (FakeWorld) UnsafeAccess.UNSAFE.allocateInstance(FakeWorld.class);
            var accessor = (WorldAccessor) worldUnsafe;
            accessor.polymc$setBiomeAccess(new BiomeAccess(worldUnsafe, 1l));
            accessor.polymc$setBorder(new WorldBorder());
            accessor.polymc$setDebugWorld(true);
            accessor.polymc$setProfiler(() -> new ProfilerSystem(() -> 0l, () -> 0, false));
            accessor.polymc$setProperties(new FakeWorldProperties());
            accessor.polymc$setRegistryKey(RegistryKey.of(RegistryKeys.WORLD, new Identifier("polymer","fake_world")));
            accessor.polymc$setDimensionKey(DimensionTypes.OVERWORLD);
            accessor.polymc$setDimensionEntry(dimType);
            accessor.polymc$setThread(Thread.currentThread());
            accessor.polymc$setRandom(Random.create());
            accessor.polymc$setAsyncRandom(Random.createThreadSafe());
            accessor.polymc$setBlockEntityTickers(new ArrayList<>());
            accessor.polymc$setPendingBlockEntityTickers(new ArrayList<>());

        } catch (Throwable e) {
            PolyMc.LOGGER.error("Creating fake world with unsafe failed...");
            e.printStackTrace();
            worldUnsafe = null;
        }

        try {
            worldDefault = new FakeWorld(
                    new FakeWorldProperties(),
                    RegistryKey.of(RegistryKeys.WORLD, new Identifier("polymer", "fake_world")),
                    dimType,
                    () -> new ProfilerSystem(() -> 0l, () -> 0, false),
                    false,
                    true,
                    1
            );
        } catch (Throwable e) {
            PolyMc.LOGGER.error("Creating fake world in regular way failed...");
            e.printStackTrace();
            worldDefault = null;
        }


        INSTANCE_UNSAFE = worldUnsafe;
        INSTANCE_REGULAR = worldDefault;

        INSTANCE = worldUnsafe != null ? worldUnsafe : worldDefault;
    }

    protected FakeWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed, 0);
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> registryEntry, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> registryEntry, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity player, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public String asString() {
        return "FakeWorld!";
    }

    @Nullable
    @Override
    public Entity getEntityById(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapState getMapState(String id) {
        return null;
    }

    @Override
    public void putMapState(String id, MapState state) {

    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return SCOREBOARD;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return RECIPE_MANAGER;
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return ENTITY_LOOKUP;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return (QueryableTickScheduler<Block>) FAKE_SCHEDULER;
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return (QueryableTickScheduler<Fluid>) FAKE_SCHEDULER;
    }

    @Override
    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void emitGameEvent(GameEvent event, Vec3d pos, @Nullable GameEvent.Emitter emitter) {

    }

    @Override
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {

    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return REGISTRY_MANAGER;
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return FEATURES;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return null;//BuiltinRegistries.BIOME.getEntry(BiomeKeys.THE_VOID).get();
    }


    static class FakeWorldProperties implements MutableWorldProperties {

        @Override
        public int getSpawnX() {
            return 0;
        }

        @Override
        public void setSpawnX(int spawnX) {

        }

        @Override
        public int getSpawnY() {
            return 0;
        }

        @Override
        public void setSpawnY(int spawnY) {

        }

        @Override
        public int getSpawnZ() {
            return 0;
        }

        @Override
        public void setSpawnZ(int spawnZ) {

        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public void setSpawnAngle(float angle) {

        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public long getTimeOfDay() {
            return 0;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return false;
        }

        @Override
        public void setRaining(boolean raining) {

        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public GameRules getGameRules() {
            return new GameRules();
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.NORMAL;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }
    }
}
