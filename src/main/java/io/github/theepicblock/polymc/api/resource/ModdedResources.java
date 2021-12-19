package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;

import java.io.InputStream;

public interface ModdedResources {
    default TextureAsset getTexture(String namespace, String texture) {
        InputStream textureStream = getInputStream(namespace, ResourceConstants.texture(texture));

        var metaPath = ResourceConstants.textureMeta(texture);
        if (this.containsAsset(namespace, metaPath)) {
            return new TextureAsset(textureStream, getInputStream(namespace, metaPath));
        } else {
            return new TextureAsset(textureStream, null);
        }
    }

    default SoundAsset getSound(String namespace, String sound) {
        var stream = getInputStream(namespace, ResourceConstants.sound(sound));
        return stream == null ? null : new SoundAsset(stream);
    }

    //TODO lang files

    default JBlockState getBlockState(String namespace, String block) {
        var stream = getInputStream(namespace, ResourceConstants.blockstate(block));
        return stream == null ? null : new JBlockStateWrapper(stream);
    }

    default JModel getItemModel(String namespace, String model) {
        return getModel(namespace, "item/"+model);
    }

    default JModel getModel(String namespace, String model) {
        var stream = getInputStream(namespace, ResourceConstants.model(model));
        return stream == null ? null : new JModelWrapper(stream);
    }

    InputStream getInputStream(String namespace, String model);

    boolean containsAsset(String namespace, String model);
}
