package io.github.theepicblock.polymc.api.resource;

import com.google.gson.Gson;
import io.github.theepicblock.polymc.api.resource.json.JBlockState;
import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.api.resource.json.JSoundEventRegistry;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
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

    /**
     * Retrieves the model of a specified asset. If the asset is not yet in the resource pack, it'll look in {@code baseResources} to copy it.
     * The intended use-case for this function is for when retrieving vanilla item models to add overrides to them.
     */
    default JModel getOrDefaultVanillaItemModel(ModdedResources baseResources, String namespace, String model, SimpleLogger logger) {
        if (this.getItemModel(namespace, model) == null) {
            var clientJar = baseResources.includeClientJar(logger);
            var defaultModel = clientJar.getItemModel(namespace, model);
            if (defaultModel == null) {
                throw new IllegalArgumentException(namespace+":"+model+" is not a valid vanilla model. Couldn't find it in jar");
            }
            this.setItemModel(namespace, model, defaultModel);
        }
        return this.getOrDefaultVanillaItemModel(namespace, model);
    }

    /**
     * Generates the model file for a vanilla asset
     * @deprecated use {@link #getOrDefaultVanillaItemModel(ModdedResources, String, String, SimpleLogger)}
     */
    @Deprecated
    default JModel getOrDefaultVanillaItemModel(String namespace, String model) {
        if (this.getItemModel(namespace, model) == null) {
            if (!Util.isNamespaceVanilla(namespace)) {
                throw new IllegalArgumentException("Don't know how to generate a default model for "+namespace+":"+model);
            }
            if (Objects.equals(model, "shield")) {
                var newModel = Util.GSON.fromJson("{\"parent\":\"builtin/entity\",\"gui_light\":\"front\",\"textures\":{\"particle\":\"block/dark_oak_planks\"},\"display\":{\"thirdperson_righthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,-4],\"scale\":[1,1,1]},\"thirdperson_lefthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,12],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,180,5],\"translation\":[-10,2,-10],\"scale\":[1.25,1.25,1.25]},\"firstperson_lefthand\":{\"rotation\":[0,180,5],\"translation\":[10,0,-10],\"scale\":[1.25,1.25,1.25]},\"gui\":{\"rotation\":[15,-25,-5],\"translation\":[2,3,0],\"scale\":[0.65,0.65,0.65]},\"fixed\":{\"rotation\":[0,180,0],\"translation\":[-2,4,-5],\"scale\":[0.5,0.5,0.5]},\"ground\":{\"rotation\":[0,0,0],\"translation\":[4,4,2],\"scale\":[0.25,0.25,0.25]}},\"overrides\":[{\"predicate\":{\"blocking\":1},\"model\":\"item/shield_blocking\"}]}", JModelImpl.class);
                this.setItemModel(namespace, model, newModel);
            } else if (Objects.equals(model, "bow")) {
                var newModel = Util.GSON.fromJson("{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"item/bow\"},\"display\":{\"thirdperson_righthand\":{\"rotation\":[-80,260,-40],\"translation\":[-1,-2,2.5],\"scale\":[0.9,0.9,0.9]},\"thirdperson_lefthand\":{\"rotation\":[-80,-280,40],\"translation\":[-1,-2,2.5],\"scale\":[0.9,0.9,0.9]},\"firstperson_righthand\":{\"rotation\":[0,-90,25],\"translation\":[1.13,3.2,1.13],\"scale\":[0.68,0.68,0.68]},\"firstperson_lefthand\":{\"rotation\":[0,90,-25],\"translation\":[1.13,3.2,1.13],\"scale\":[0.68,0.68,0.68]}},\"overrides\":[{\"predicate\":{\"pulling\":1},\"model\":\"item/bow_pulling_0\"},{\"predicate\":{\"pulling\":1,\"pull\":0.65},\"model\":\"item/bow_pulling_1\"},{\"predicate\":{\"pulling\":1,\"pull\":0.9},\"model\":\"item/bow_pulling_2\"}]}", JModelImpl.class);
                this.setItemModel(namespace, model, newModel);
            } else {
                var newModel = new JModelImpl();
                newModel.setParent("item/generated");
                if (Objects.equals(model, "stick")) {
                    newModel.setParent("item/handheld");
                }
                newModel.getTextures().put("layer0", "item/"+model);

                if (ArrayUtils.contains(new String[]{"leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots"}, model)) {
                    newModel.getTextures().put("layer1", "item/"+model+"_overlay");
                }

                this.setItemModel(namespace, model, newModel);
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
