package io.github.theepicblock.polymc.mixins.tag;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TagGroup.Serialized.class)
public interface SerializedAccessor {
    @Invoker("<init>")
    static TagGroup.Serialized createNew(Map<Identifier,IntList> contents) {
        throw new IllegalStateException();
    }
}
