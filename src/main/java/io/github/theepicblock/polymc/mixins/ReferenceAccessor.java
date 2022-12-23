package io.github.theepicblock.polymc.mixins;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryEntry.Reference.class)
public interface ReferenceAccessor {
    @Invoker
    void callSetRegistryKey(RegistryKey<?> registryKey);
}
