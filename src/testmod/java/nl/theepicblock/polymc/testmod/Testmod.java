package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class Testmod implements ModInitializer {
    private static final String MODID = "polymc-testmod";

    public static final Item TEST_ITEM = new TestItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(6).rarity(Rarity.EPIC));
    public static final Block TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.SOIL, MapColor.BLUE));
    public static final Block TEST_BLOCK_GLOWING = new Block(FabricBlockSettings.of(Material.AMETHYST, MapColor.RAW_IRON_PINK).luminance(9));
    public static final Block TEST_BLOCK_WIZARD = new FallingBlock(FabricBlockSettings.of(Material.GLASS, MapColor.CYAN));

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, id("test_item"), TEST_ITEM);

        registerBlock(id("test_block"), TEST_BLOCK);
        registerBlock(id("test_block_glowing"), TEST_BLOCK_GLOWING);
        registerBlock(id("test_block_wizard"), TEST_BLOCK_WIZARD);

        Registry.register(Registry.ENCHANTMENT, id("test_enchantment"), new TestEnchantment());
    }

    public static void debugSend(@Nullable PlayerEntity playerEntity, String text) {
        if (playerEntity != null) playerEntity.sendMessage(new LiteralText(text), false);
    }

    private static void registerBlock(Identifier id, Block block) {
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings()));
    }

    private static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
