package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JSoundEventRegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface ResourceContainer {
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

    boolean containsAsset(String namespace, String path);
}
