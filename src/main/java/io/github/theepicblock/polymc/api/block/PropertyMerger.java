package io.github.theepicblock.polymc.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import java.util.function.Predicate;

public class PropertyMerger<T extends Comparable<T>> implements BlockStateMerger {
    private final Predicate<BlockState> activation;
    private final Property<T> property;
    private final T defaultValue;

    public PropertyMerger(Property<T> property) {
        this(property, (state) -> true);
    }

    public PropertyMerger(Property<T> property, Predicate<BlockState> activation) {
        this.activation = activation;
        this.property = property;
        this.defaultValue = property.getValues().iterator().next();
    }

    @Override
    public BlockState normalize(BlockState state) {
        if (activation.test(state) && state.contains(property)) {
            return state.with(property, defaultValue);
        } else {
            return state;
        }
    }
}
