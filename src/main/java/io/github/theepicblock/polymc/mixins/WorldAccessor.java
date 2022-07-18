package io.github.theepicblock.polymc.mixins;

import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
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
    @Accessor
    void setThread(Thread thread);

    @Mutable
    @Accessor
    void setDebugWorld(boolean debugWorld);

    @Mutable
    @Accessor
    void setProperties(MutableWorldProperties properties);

    @Mutable
    @Accessor
    void setProfiler(Supplier<Profiler> profiler);

    @Mutable
    @Accessor
    void setBorder(WorldBorder border);

    @Mutable
    @Accessor
    void setBiomeAccess(BiomeAccess biomeAccess);

    @Mutable
    @Accessor
    void setRegistryKey(RegistryKey<World> registryKey);

    @Mutable
    @Accessor
    void setDimension(RegistryKey<DimensionType> dimension);

    @Mutable
    @Accessor
    void setDimensionEntry(RegistryEntry<DimensionType> dimensionEntry);

    @Mutable
    @Accessor
    void setRandom(Random random);

    @Mutable
    @Accessor
    void setThreadSafeRandom(Random random);

    @Mutable
    @Accessor
    void setBlockEntityTickers(List<BlockEntityTickInvoker> list);

    @Mutable
    @Accessor
    void setPendingBlockEntityTickers(List<BlockEntityTickInvoker> list);
}
