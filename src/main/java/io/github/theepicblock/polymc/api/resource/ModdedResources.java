package io.github.theepicblock.polymc.api.resource;

import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;
import io.github.theepicblock.polymc.impl.resource.json.JSoundEventRegistryWrapper;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ModdedResources extends AutoCloseable {
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

    /**
     * @param path should always be "sounds.json"
     */
    default JSoundEventRegistry getSoundRegistry(String namespace, String path) {
        var stream = getInputStream(namespace, path);
        return stream == null ? null : new JSoundEventRegistryWrapper(stream);
    }

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

    InputStream getInputStream(String namespace, String path);

    /**
     * Gets all the files registered to this namespace:path. This is useful if a file can be defined in multiple places and need to be merged.
     * For example, there can be multiple 'minecraft:lang/en_us.json' definitions. The vanilla client will merge all of these
     */
    List<InputStream> getInputStreams(String namespace, String path);

    /**
     * @return all namespaces that are in this combined resource pack
     */
    Set<String> getAllNamespaces();

    Set<Identifier> locateLanguageFiles();

    boolean containsAsset(String namespace, String model);
}
