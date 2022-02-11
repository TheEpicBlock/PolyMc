package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JSoundEventRegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ModdedResources extends AutoCloseable {
    default @Nullable TextureAsset getTexture(String namespace, String texture) {
        InputStream textureStream = getInputStream(namespace, ResourceConstants.texture(texture));

        if (textureStream == null) return null;

        var metaPath = ResourceConstants.textureMeta(texture);
        if (this.containsAsset(namespace, metaPath)) {
            return new TextureAsset(textureStream, getInputStream(namespace, metaPath));
        } else {
            return new TextureAsset(textureStream, null);
        }
    }

    default @Nullable SoundAsset getSound(String namespace, String sound) {
        var stream = getInputStream(namespace, ResourceConstants.sound(sound));
        return stream == null ? null : new SoundAsset(stream);
    }

    /**
     * @param path should always be "sounds.json"
     */
    default @Nullable JSoundEventRegistry getSoundRegistry(String namespace, String path) {
        var stream = getInputStream(namespace, path);
        return stream == null ? null : new JSoundEventRegistryWrapper(stream, namespace+":"+path);
    }

    default @Nullable JBlockState getBlockState(String namespace, String block) {
        var path = ResourceConstants.blockstate(block);
        var stream = getInputStream(namespace, path);
        return stream == null ? null : new JBlockStateWrapper(stream, namespace+":"+path);
    }

    default @Nullable JModel getItemModel(String namespace, String model) {
        return getModel(namespace, "item/"+model);
    }

    default @Nullable JModel getModel(String namespace, String model) {
        var path = ResourceConstants.model(model);
        var stream = getInputStream(namespace, path);
        return stream == null ? null : new JModelWrapper(stream, namespace+":"+path);
    }

    @Nullable InputStream getInputStream(String namespace, String path);

    /**
     * Gets all the files registered to this namespace:path. This is useful if a file can be defined in multiple places and need to be merged.
     * For example, there can be multiple 'minecraft:lang/en_us.json' definitions. The vanilla client will merge all of these
     */
    @NotNull List<InputStream> getInputStreams(String namespace, String path);

    /**
     * @return all namespaces that are in this combined resource pack
     */
    @NotNull Set<String> getAllNamespaces();

    @NotNull Set<Identifier> locateLanguageFiles();

    boolean containsAsset(String namespace, String model);
}
