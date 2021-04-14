package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class will automatically call a builder function when a polymap is attempted to be accessed that doesn't exist.
 */
public class PolyMapMap<K> extends Reference2ObjectArrayMap<PolyMap, K> {
	private final Function<PolyMap, K> builder;

	public PolyMapMap(Function<PolyMap, K> builder) {
		super(2);
		this.builder = builder;
	}

	@Override
	public K get(Object k) {
		K returnValue = super.get(k);

		if (returnValue == null && builder != null) {
			returnValue = builder.apply((PolyMap)k);
			this.put((PolyMap)k, returnValue);
		}

		return returnValue;
	}
}
