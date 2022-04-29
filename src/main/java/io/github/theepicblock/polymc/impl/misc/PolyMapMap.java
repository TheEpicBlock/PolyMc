package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.api.PolyMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.function.Function;

/**
 * This class will automatically call a builder function when a polymap is attempted to be accessed that doesn't exist.
 */
public class PolyMapMap<K> extends Reference2ObjectArrayMap<PolyMap,K> {
    private final Function<PolyMap,K> builder;

    public PolyMapMap(Function<PolyMap,K> builder) {
        super(2);
        this.builder = builder;
    }

    @Override
    public K get(Object k) {
        if (!this.containsKey(k)) {
            this.put((PolyMap)k, builder.apply((PolyMap)k));
        }

        return super.get(k);
    }
}
