package io.github.theepicblock.polymc.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import java.util.function.BiFunction;

/**
 * It's recommended to read the comments inside the code of {@link io.github.theepicblock.polymc.impl.poly.block.FunctionBlockStatePoly#FunctionBlockStatePoly(Block, BiFunction, BlockStateMerger)}, which is where the merger will be used.
 */
@FunctionalInterface
public interface BlockStateMerger {
    BlockStateMerger DEFAULT = new PropertyMerger<>(Properties.STAGE)
            .combine(new PropertyMerger<>(Properties.DISTANCE_1_7))
            .combine(new PropertyMerger<>(Properties.DISTANCE_0_7))
            .combine(new PropertyMerger<>(Properties.AGE_15))
            .combine(new PropertyMerger<>(Properties.POWERED))
            .combine(new PropertyMerger<>(Properties.TRIGGERED))
            .combine(new PropertyMerger<>(Properties.PERSISTENT))
            .combine(new PropertyMerger<>(Properties.NOTE))
            .combine(new PropertyMerger<>(Properties.INSTRUMENT))
            .combine((state) -> {
                if (state.contains(Properties.MOISTURE)) {
                    // Moisture lower than 7 are the same
                    if (state.get(Properties.MOISTURE) < 7) {
                        return state.with(Properties.MOISTURE, 0);
                    }
                }
                return state;
            }).combine((state) -> {
                if (state.contains(Properties.SLAB_TYPE) && state.contains(Properties.WATERLOGGED)) {
                    // Waterlogged double slabs do not need to exist
                    if (state.get(Properties.SLAB_TYPE) == SlabType.DOUBLE && state.get(Properties.WATERLOGGED)) {
                        return state.with(Properties.WATERLOGGED, false);
                    }
                }
                return state;
            });
    BlockStateMerger ALL = (a) -> {
        for (var property : a.getProperties()) {
            a = normalizeProperty(a, property);
        }
        return a;
    };

    BlockState normalize(BlockState b);

    default BlockStateMerger combine(BlockStateMerger other) {
        return (a) -> this.normalize(other.normalize(a));
    }

    static <T extends Comparable<T>> BlockState normalizeProperty(BlockState state, Property<T> property) {
        return state.with(property, property.getValues().iterator().next());
    }
}
