package io.github.theepicblock.polymc.mixins.tag;

import io.github.theepicblock.polymc.impl.mixin.SerializedMixinDuck;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * This patch prevents modded blocks/items from appearing in tags when they are synchronised to the client.
 * The client will replace any ids it doesn't recognize with minecraft:air.
 * This can cause issues. For example: a mod places a block in the {@link net.minecraft.tag.BlockTags#CLIMBABLE} tag.
 * It gets replaced with minecraft:air and the client now thinks it can climb air.
 * <p>
 * Note: we're not actually mixing into the interface, but we're mixing into the anonymous class in {@link TagGroup#create(Map)}
 * This is because we can't mixin to default methods.
 * </p>
 */
@Mixin(targets = "net/minecraft/tag/TagGroup$1")
public abstract class TagGroupMixin<T> implements TagGroup<T> {
    @Shadow public abstract Map<Identifier,Tag<T>> getTags();

    /**
     * Usually this method passes a map of (tag identifier -> list of raw ids in the tag) to the {@link net.minecraft.tag.TagGroup.Serialized} object.
     * The id lookup is deferred to be done in {@link SerializedMixin}. We just pass in the original list.
     */
    public TagGroup.Serialized serialize(Registry<T> registry) {
        var obj = SerializedAccessor.createNew(null);

        ((SerializedMixinDuck<T>)obj).setTags(this.getTags());
        ((SerializedMixinDuck<T>)obj).setRegistry(registry);

        return obj;
    }
}
