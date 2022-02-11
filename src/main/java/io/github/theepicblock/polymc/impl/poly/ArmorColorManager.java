package io.github.theepicblock.polymc.impl.poly;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.SharedValuesKey;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.item.ArmorMaterial;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Objects;

public class ArmorColorManager implements SharedValuesKey.ResourceContainer {
    public static final SharedValuesKey<ArmorColorManager> KEY = new SharedValuesKey<>(ArmorColorManager::new, (it) -> it);

    private final Object2IntArrayMap<ArmorMaterial> colors = new Object2IntArrayMap<>();

    public ArmorColorManager() {

    }

    public ArmorColorManager(PolyRegistry registry) {
        this();
    }

    public int getColorForMaterial(ArmorMaterial material) {
        return colors.computeIfAbsent(material, (key) -> 0xFFFFFF - colors.size() * 2);
    }

    // We don't have any reference to the PolyRegistry, so the resource container can be the same class
    @Override
    public void addToResourcePack(ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        if (this.colors.isEmpty()) {
            return;
        }

        // Do this entire thing twice for both armor layers
        for (int layer = 1; layer <= 2; layer++) {
            int outputWidth = 64;
            int outputHeight = 32;

            var moddedTextures = new HashMap<ArmorMaterial, BufferedImage>();

            // Collect all modded textures and calculate the size of the output
            for (var material : colors.keySet()) {
                try {
                    var texturePath = "models/armor/" + material.getName() + "_layer_" + layer;
                    var texture = moddedResources.getTexture("minecraft", texturePath);
                    if (texture == null) continue;

                    var moddedImage = ImageIO.read(texture.getTexture());
                    if (moddedImage == null) continue;

                    moddedTextures.put(material, moddedImage);
                    outputHeight = Math.max(outputHeight, moddedImage.getHeight());
                    outputWidth += moddedImage.getWidth();
                } catch (IOException e) {
                    logger.error("Couldn't read armor texture "+material.getName()+" (layer #"+layer+")");
                    e.printStackTrace();
                }
            }

            var image = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
            var graphics = image.getGraphics();

            // Write vanilla leather
            try {
                var leatherTexture = ImageIO.read(Objects.requireNonNull(PolyMc.class.getResourceAsStream("/fancypants/leather_layer_" + layer + ".png")));
                graphics.drawImage(leatherTexture, 0, 0, null);

                var leatherOverlayTexture = ImageIO.read(Objects.requireNonNull(PolyMc.class.getResourceAsStream("/fancypants/leather_layer_" + layer + "_overlay.png")));
                graphics.drawImage(leatherOverlayTexture, 0, 0, null);
            } catch (Exception e) {
                logger.error("Error reading vanilla armor textures, please report this");
                e.printStackTrace();
                return;
            }

            graphics.setColor(Color.WHITE);
            graphics.drawRect(0, 1, 0, 0);

            // Write the modded textures to the output image
            var xIndex = 64;
            for (var entry : colors.object2IntEntrySet()) {
                var material = entry.getKey();
                var color = entry.getIntValue();
                var texture = moddedTextures.get(material);

                graphics.drawImage(texture, xIndex, 0, null);

                graphics.setColor(new Color(color));
                graphics.drawRect(xIndex, 0, 0, 0);

                xIndex += texture.getWidth();
            }

            // Add the output image to the resource pack
            pack.setAsset("minecraft", "textures/models/armor/leather_layer_"+layer+".png", (path, gson) -> {
                ImageIO.write(image, "png", path.toFile());
            });

            // Write something small to the overlay texture because apparently that's needed
            pack.setAsset("minecraft", "textures/models/armor/leather_layer_"+layer+"_overlay.png", (path, gson) -> {
                ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", path.toFile());
            });
        }

        try {
            // Actually put the FancyPants shader files in the resource pack
            for (String string : new String[]{"fsh", "json", "vsh"}) {
                var stream = PolyMc.class.getResourceAsStream("/fancypants/rendertype_armor_cutout_no_cull." +string);
                assert stream != null;

                pack.setAsset("minecraft", "shaders/core/rendertype_armor_cutout_no_cull." + string, (path, gson) -> {
                    Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
                });
            }
        } catch (Exception e) {
            logger.warn("Error occurred when writing armor shader!");
            e.printStackTrace();
        }
    }
}
