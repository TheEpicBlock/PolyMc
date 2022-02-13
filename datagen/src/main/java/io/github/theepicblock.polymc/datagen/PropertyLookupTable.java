package io.github.theepicblock.polymc.datagen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Property;

import java.util.Set;

public class PropertyLookupTable {
    private final Object2IntMap<Property<?>> property2Int = new Object2IntOpenHashMap<>();
    private final Object2IntMap<?>[] propertyValues2Int;

    public PropertyLookupTable(Set<Property<?>> properties) {
        this.propertyValues2Int = new Object2IntMap<?>[properties.size()];
        var i = 0;
        for (var property : properties) {
            property2Int.put(property, i);
            var valueMap = new Object2IntOpenHashMap<>();
            var j = 0;
            for (var value : property.getValues()) {
                valueMap.put(value, j);
                j++;
            }
            propertyValues2Int[i] = valueMap;
            i++;
        }
    }

    public void write(PacketByteBuf buf) {
        var propertyArray = new Property<?>[property2Int.size()];
        property2Int.forEach((property, integer) -> {
            propertyArray[integer] = property;
        });

        buf.writeVarInt(propertyArray.length);
        for (int i = 0; i < propertyArray.length; i++) {
            var property = propertyArray[i];
            buf.writeString(property.getName());

            var values2int = propertyValues2Int[i];
            var values2intArray = new Object[values2int.size()];
            values2int.forEach((value, integer) -> {
                values2intArray[integer] = value;
            });

            buf.writeVarInt(values2int.size());
            for (var value : values2intArray) {
                buf.writeString(getValueName(property, value));
            }
        }
    }

    private static <T extends Comparable<T>> String getValueName(Property<T> property, Object value) {
        return property.name((T)value);
    }

    public int getPropertyId(Property<?> property) {
        return property2Int.getInt(property);
    }

    public <T extends Comparable<T>> int getValueId(Property<?> property, T value) {
        return propertyValues2Int[getPropertyId(property)].getInt(value);
    }
}
