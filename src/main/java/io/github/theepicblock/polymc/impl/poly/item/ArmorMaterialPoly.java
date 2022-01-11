package io.github.theepicblock.polymc.impl.poly.item;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import static io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator.getPolymcPath;

public class ArmorMaterialPoly {

    private Identifier modelPath;
    private ArmorMaterial material;
    private int colorId;
    private int number;

    public ArmorMaterialPoly(ArmorMaterial material) {
        this.material = material;

        // New ArmorMaterial textures are probably put in the `minecraft` namespace
        this.setModelPath(new Identifier("minecraft", material.getName()));
    }

    /**
     * Set the number to use for this armor
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Get the number to use for this armor
     */
    public Integer getNumber() {
        return this.number;
    }

    /**
     * Set the unique color to use for this armor
     * @param colorId
     */
    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    /**
     * Get the unique color to use for this armor
     */
    public Integer getColorId() {
        return this.colorId;
    }

    /**
     * Set the path to the original model texture
     */
    public void setModelPath(Identifier modelPath) {
        this.modelPath = modelPath;
    }

    /**
     * Get the path to the original model texture
     */
    public Identifier getModelPath() {
        return this.modelPath;
    }

    /**
     * Add to the resource pack.
     * This is empty by default because this base class will only use FancyPants,
     * which will then call the `addToResourcePack` static method
     */
    public void addToResourcePack(ResourcePackMaker pack) {}

    /**
     * Use the FancyPants way of adding these armor materials?
     * (When this returns false, the non-static `addToResourcePack` method will be used when generating the resources)
     */
    public boolean shouldUseFancyPants() {
        return true;
    }

    /**
     * See if we should use the FancyPants way of adding these ArmorMaterials
     */
    public static boolean shouldUseFancyPants(ImmutableMap<ArmorMaterial, ArmorMaterialPoly> materialPolys) {

        if (materialPolys.isEmpty()) {
            return false;
        }

        for (ArmorMaterialPoly materialPoly : materialPolys.values()) {
            if (!materialPoly.shouldUseFancyPants()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add all the ArmorMaterial textures to the ResourcePack in a FancyPants kind of way
     * @param materialPolys
     * @param pack
     * @param logger
     * @throws IOException
     */
    public static void addToResourcePack(ImmutableMap<ArmorMaterial, ArmorMaterialPoly> materialPolys, ResourcePackMaker pack, SimpleLogger logger) throws IOException {

        if (materialPolys.isEmpty()) {
            return;
        }

        // We need to modify the original leather armor texture,
        // so we need to get the client jar for that
        ZipFile clientJar = ResourcePackMaker.getClientJar();

        if (clientJar == null) {
            logger.error("Could not find the Minecraft client jar, unable to generate armor textures");
            return;
        }

        var list = new ArrayList<Triple<Integer, BufferedImage[], ArmorMaterialPoly[]>>();

        int[] width = new int[]{64, 64};
        int[] height = new int[]{32, 32};
        int counter = -1;

        for (ArmorMaterialPoly materialPoly : materialPolys.values()) {
            counter++;

            Identifier modelPath = materialPoly.getModelPath();

            try {
                var image = new BufferedImage[2];
                var b = new ArmorMaterialPoly[2];
                BufferedImage bi = null;

                for (int i = 0; i <= 1; i++) {
                    var path = "assets/" + modelPath.getNamespace() + "/textures/models/armor/" + modelPath.getPath() + "_layer_" + (i + 1) + ".png";
                    byte[] data = pack.getFileStream(modelPath.getNamespace(), path).readAllBytes();

                    if (data != null) {
                        bi = ImageIO.read(new ByteArrayInputStream(data));
                    }

                    if (bi != null) {
                        height[i] = Math.max(height[i], bi.getHeight());
                        width[i] += bi.getWidth();
                    }

                    image[i] = bi;
                    b[i] = materialPoly;
                }

                list.add(Triple.of(counter, image, b));
            } catch (Exception e) {
                logger.warn("Error occurred when creating " + modelPath + " armor texture!");
                e.printStackTrace();
            }
        }

        var image = new BufferedImage[]{new BufferedImage(width[0], height[0], BufferedImage.TYPE_INT_ARGB), new BufferedImage(width[1], height[1], BufferedImage.TYPE_INT_ARGB)};
        int[] cWidth = new int[]{64, 64};

        var graphics = new Graphics[]{image[0].getGraphics(), image[1].getGraphics()};

        for (int i = 0; i <= 1; i++) {
            BufferedImage tex;

            tex = ImageIO.read(clientJar.getInputStream(clientJar.getEntry(leatherLayerPath(i + 1))));
            graphics[i].drawImage(tex, 0, 0, null);

            tex = ImageIO.read(clientJar.getInputStream(clientJar.getEntry(leatherLayerOverlayPath(i + 1))));
            graphics[i].drawImage(tex, 0, 0, null);

            graphics[i].setColor(Color.WHITE);
            graphics[i].drawRect(0, 1, 0, 0);
        }

        for (var entry : list) {
            for (int i = 0; i <= 1; i++) {
                ArmorMaterialPoly metadata = entry.getRight()[i];

                graphics[i].drawImage(entry.getMiddle()[i], cWidth[i], 0, null);

                graphics[i].setColor(new Color(metadata.getColorId()));
                graphics[i].drawRect(cWidth[i], 0, 0, 0);

                cWidth[i] += entry.getMiddle()[i].getWidth();
            }
        }

        try {
            for (int i = 0; i <= 1; i++) {
                graphics[i].dispose();
                ByteArrayOutputStream out;

                out = new ByteArrayOutputStream();
                ImageIO.write(image[i], "png", out);
                pack.writeToPath(leatherLayerPath(i + 1), out.toByteArray());

                out = new ByteArrayOutputStream();
                ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png", out);
                pack.writeToPath(leatherLayerOverlayPath(i + 1), out.toByteArray());

            }
        } catch (Exception e) {
            logger.warn("Error occurred when writing leather armor texture!");
            e.printStackTrace();
        }

        try {
            // Actually put the FancyPants shader files in the resource pack
            for (String string : new String[]{"fsh", "json", "vsh"}) {
                pack.writeToPath(
                        "assets/minecraft/shaders/core/rendertype_armor_cutout_no_cull." + string,
                        Files.readAllBytes(getPolymcPath("base-armor/rendertype_armor_cutout_no_cull." + string))
                );
            }
        } catch (Exception e) {
            logger.warn("Error occurred when writing armor shader!");
            e.printStackTrace();
        }
    }

    /**
     * Construct a path to the leather layer of the given level
     * @param level
     */
    private static String leatherLayerPath(int level) {
        return "assets/minecraft/textures/models/armor/leather_layer_" + level + ".png";
    }

    /**
     * Construct a path to the overlay leather layer of the given level
     * @param level
     */
    private static String leatherLayerOverlayPath(int level) {
        return "assets/minecraft/textures/models/armor/leather_layer_" + level + "_overlay.png";
    }
}
