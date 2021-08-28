package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

/**
 * @see io.github.theepicblock.polymc.mixins.tag.SerializedMixin
 */
public interface SerializedMixinDuck<T> {
    void setTags(Map<Identifier,Tag<T>> v);
    void setRegistry(Registry<T> v);
}
