package io.github.theepicblock.polymc.mixins.wizards;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("MAX_ENTITY_ID")
    static AtomicInteger getMaxEntityId() {
        throw new IllegalStateException();
    }
}
