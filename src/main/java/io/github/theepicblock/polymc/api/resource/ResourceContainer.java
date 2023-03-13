package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateImpl;
import io.github.theepicblock.polymc.impl.resource.json.JModelImpl;
import io.github.theepicblock.polymc.impl.resource.json.JSoundEventRegistryImpl;
import net.minecraft.resource.InputSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceContainer {
    default @Nullable TextureAsset getTexture(String namespace, String texture) {
        var textureStream = getInputStreamSupplier(namespace, ResourceConstants.texture(texture));
        if (textureStream == null) return null;

        var metaPath = ResourceConstants.textureMeta(texture);
        return new TextureAsset(textureStream, getInputStreamSupplier(namespace, metaPath));
    }

    default @Nullable SoundAsset getSound(String namespace, String sound) {
        var stream = getInputStreamSupplier(namespace, ResourceConstants.sound(sound));
        return stream == null ? null : new SoundAsset(stream);
    }

    /**
     * @param path should always be "sounds.json"
     */
    default @Nullable JSoundEventRegistry getSoundRegistry(String namespace, String path) {
        var stream = getInputStream(namespace, path);
        return stream == null ? null : JSoundEventRegistryImpl.of(stream, namespace+":"+path);
    }

    default @Nullable JBlockState getBlockState(String namespace, String block) {
        var path = ResourceConstants.blockstate(block);
        var stream = getInputStream(namespace, path);
        return stream == null ? null : JBlockStateImpl.of(stream, namespace+":"+path);
    }

    default @Nullable JModel getItemModel(String namespace, String model) {
        return getModel(namespace, "item/"+model);
    }

    default @Nullable JModel getModel(String namespace, String model) {
        var path = ResourceConstants.model(model);
        var stream = getInputStream(namespace, path);
        return stream == null ? null : JModelImpl.of(stream, namespace+":"+path);
    }

    @Nullable InputSupplier<InputStream> getInputStreamSupplier(String namespace, String path);

    default InputStream getInputStream(String namespace, String path) {
        var supplier = getInputStreamSupplier(namespace, path);
        if (supplier == null) {
            return null;
        } else {
            try {
                return supplier.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean containsAsset(String namespace, String path);
}
