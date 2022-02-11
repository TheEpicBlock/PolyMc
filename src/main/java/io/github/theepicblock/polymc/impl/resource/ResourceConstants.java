package io.github.theepicblock.polymc.impl.resource;

import net.minecraft.util.Identifier;

public class ResourceConstants {
    public static final String ASSETS = "assets/";
    public static final String MODELS = "models/";
    public static final String TEXTURES = "textures/";
    public static final String SOUNDS = "sounds/";
    public static final String BLOCKSTATES = "blockstates/";

    public static String model(String in) {
        return MODELS + in + ".json";
    }

    public static String texture(String in) {
        return TEXTURES + in + ".png";
    }

    public static String textureMeta(String in) {
        return TEXTURES + in + ".png.mcmeta";
    }

    public static String sound(String in) {
        return SOUNDS + in + ".ogg";
    }

    public static String blockstate(String in) {
        return BLOCKSTATES + in + ".json"; //FIXME
    }

    public static String itemLocation(Identifier item) {
        return item.getNamespace() + ":" + "item/" + item.getPath();
    }
}
