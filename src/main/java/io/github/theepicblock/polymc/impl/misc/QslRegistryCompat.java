package io.github.theepicblock.polymc.impl.misc;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

import java.lang.reflect.InvocationTargetException;

public class QslRegistryCompat {
    public static void init() {
        try {
            var method = RegistrySynchronization.class.getMethod("setRegistryOptional", SimpleRegistry.class);
            for (var registry : Registry.REGISTRIES) {
                if (registry instanceof SimpleRegistry simpleRegistry) {
                    method.invoke(null, simpleRegistry);
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            PolyMc.LOGGER.info("Exception in qsl compatibility");
            e.printStackTrace();
        }
    }
}
