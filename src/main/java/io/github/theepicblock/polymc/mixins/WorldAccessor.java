package io.github.theepicblock.polymc.mixins;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

@Mixin(World.class)
public interface WorldAccessor {
    @Mutable
    @Accessor("thread")
    void polymc$setThread(Thread thread);

    @Mutable
    @Accessor("debugWorld")
    void polymc$setDebugWorld(boolean debugWorld);

    @Mutable
    @Accessor("properties")
    void polymc$setProperties(MutableWorldProperties properties);

    @Mutable
    @Accessor("profiler")
    void polymc$setProfiler(Supplier<Profiler> profiler);

    @Mutable
    @Accessor("border")
    void polymc$setBorder(WorldBorder border);

    @Mutable
    @Accessor("biomeAccess")
    void polymc$setBiomeAccess(BiomeAccess biomeAccess);

    @Mutable
    @Accessor("registryKey")
    void polymc$setRegistryKey(RegistryKey<World> registryKey);

    @Mutable
    @Accessor("dimension")
    void polymc$setDimensionKey(RegistryKey<DimensionType> dimension);

    @Mutable
    @Accessor("dimensionEntry")
    void polymc$setDimensionEntry(RegistryEntry<DimensionType> dimensionEntry);

    @Mutable
    @Accessor("random")
    void polymc$setRandom(Random random);

    @Mutable
    @Accessor("threadSafeRandom")
    void polymc$setAsyncRandom(Random random);

    @Mutable
    @Accessor("blockEntityTickers")
    void polymc$setBlockEntityTickers(List<BlockEntityTickInvoker> list);

    @Mutable
    @Accessor("pendingBlockEntityTickers")
    void polymc$setPendingBlockEntityTickers(List<BlockEntityTickInvoker> list);
}
