package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ModdedResourceContainerImpl;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import io.github.theepicblock.polymc.impl.resource.json.JBlockStateImpl;
import io.github.theepicblock.polymc.impl.resource.json.JModelImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public interface PolyMcResourcePack {
    //TODO javadoc for all these

    default void importRequirements(ModdedResources input, AssetWithDependencies[] assets, SimpleLogger logger) {
        if (assets == null) return;
        for (var asset : assets) {
            importRequirements(input, asset, logger);
        }
    }

    void importRequirements(ModdedResources input, AssetWithDependencies asset, SimpleLogger logger);

    default void setTexture(String namespace, String path, TextureAsset texture) {
        setAsset(namespace, ResourceConstants.texture(path), texture);
    }

    default void setSound(String namespace, String path, SoundAsset sound) {
        setAsset(namespace, ResourceConstants.sound(path), sound);
    }

    /**
     * @param path should always be "sounds.json"
     */
    default void setSoundRegistry(String namespace, String path, JSoundEventRegistry registry) {
        setAsset(namespace, path, registry);
    }

    default void setBlockState(String namespace, String path, JBlockState blockState) {
        setAsset(namespace, ResourceConstants.blockstate(path), blockState);
    }

    default void setItemModel(String namespace, String path, JModel itemModel) {
        setModel(namespace, "item/"+path, itemModel);
    }

    default void setModel(String namespace, String path, JModel model) {
        setAsset(namespace, ResourceConstants.model(path), model);
    }

    void setAsset(String namespace, String path, PolyMcAsset asset);

    ///

    default @Nullable TextureAsset getTexture(String namespace, String texture) {
        return (TextureAsset)this.getAsset(namespace, ResourceConstants.texture(texture));
    }

    default @Nullable SoundAsset getSound(String namespace, String sound) {
        return (SoundAsset)this.getAsset(namespace, ResourceConstants.sound(sound));
    }

    /**
     * @param path should always be "sounds.json"
     */
    default @Nullable JSoundEventRegistry getSoundRegistry(String namespace, String path) {
        return (JSoundEventRegistry)this.getAsset(namespace, path);
    }

    default @Nullable JBlockState getBlockState(String namespace, String block) {
        return (JBlockState)this.getAsset(namespace, ResourceConstants.blockstate(block));
    }

    /**
     * Utility that inserts an empty {@link JBlockState} definition (one with no variants) into the map
     * if there's no asset registered at this path,
     */
    default @Nullable JBlockState getOrDefaultBlockState(String namespace, String block) {
        if (this.getBlockState(namespace, block) == null) {
            this.setBlockState(namespace, block, new JBlockStateImpl());
        }
        return this.getBlockState(namespace, block);
    }

    default @Nullable JModel getItemModel(String namespace, String model) {
        return getModel(namespace, "item/"+model);
    }

    default JModel getOrDefaultVanillaItemModel(String namespace, String model) {
        if (this.getItemModel(namespace, model) == null) {
            var moddedResources = new ModdedResourceContainerImpl();
            ModdedResources mergedResources = moddedResources.includeClientJar(PolyMc.LOGGER);

            JModel clientModel = mergedResources.getItemModel(namespace, model);
            JModelImpl newModel;

            if (clientModel == null) {
                newModel = new JModelImpl();
                newModel.setParent("item/generated");
                if (Objects.equals(model, "stick")) {
                    newModel.setParent("item/handheld");
                }
                newModel.getTextures().put("layer0", "item/"+model);
            } else {
                newModel = new JModelImpl(clientModel);
            }

            if (ArrayUtils.contains(new String[]{"leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots"}, model)) {
                newModel.getTextures().put("layer1", "item/"+model+"_overlay");
            }

            this.setItemModel(namespace, model, newModel);

            try {
                moddedResources.close();
                mergedResources.close();
            } catch (Exception e) {
                e.printStackTrace();
                PolyMc.LOGGER.error("Failed to close modded resources");
            }
        }
        return this.getItemModel(namespace, model);
    }

    default @Nullable JModel getModel(String namespace, String model) {
        return (JModel)this.getAsset(namespace, ResourceConstants.model(model));
    }

    @Nullable PolyMcAsset getAsset(String namespace, String path);

    void write(Path location, SimpleLogger logger);

    /**
     * @param consumer consumes the namespace, path and the asset of each asset in this resource pack
     */
    void forEachAsset(TriConsumer<String, String, PolyMcAsset> consumer);

    @NotNull Gson getGson();
}
