package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class Testmod implements ModInitializer {
    private static final String MODID = "polymc-testmod";

    public static final Item TEST_ITEM = new TestItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(6).rarity(Rarity.EPIC));
    public static final Item TEST_FOOD = new Item(new FabricItemSettings().group(ItemGroup.FOOD).food(FoodComponents.COOKED_CHICKEN));
    public static final ArmorMaterial TEST_MATERIAL = new TestArmorMaterial();
    public static final Item TELMET = new ArmorItem(TEST_MATERIAL, EquipmentSlot.HEAD, new Item.Settings().group(ItemGroup.TRANSPORTATION));
    public static final Item TESTPLATE = new ArmorItem(TEST_MATERIAL, EquipmentSlot.CHEST, new Item.Settings().group(ItemGroup.TRANSPORTATION));
    public static final Item TEGGINGS = new ArmorItem(TEST_MATERIAL, EquipmentSlot.LEGS, new Item.Settings().group(ItemGroup.TRANSPORTATION));
    public static final Item TOOTS = new ArmorItem(TEST_MATERIAL, EquipmentSlot.FEET, new Item.Settings().group(ItemGroup.TRANSPORTATION));

    public static final Block TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.SOIL, MapColor.BLUE));
    public static final Block TEST_STAIRS = new TestStairsBlock(TEST_BLOCK.getDefaultState(), FabricBlockSettings.of(Material.SOIL, MapColor.BLUE));
    public static final Block TEST_SLAB = new TestSlabBlock(FabricBlockSettings.of(Material.SOIL, MapColor.BLUE));
    public static final Block TEST_DOOR = new TestDoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_DOOR));
    public static final Block TEST_TRAP_DOOR = new TestTrapdoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    public static final Block TEST_BLOCK_GLOWING = new Block(FabricBlockSettings.of(Material.AMETHYST, MapColor.RAW_IRON_PINK).luminance(9));
    public static final Block TEST_BLOCK_WIZARD = new FallingBlock(FabricBlockSettings.of(Material.GLASS, MapColor.CYAN));

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, id("test_item"), TEST_ITEM);
        Registry.register(Registry.ITEM, id("test_food"), TEST_FOOD);

        Registry.register(Registry.ITEM, id("test_helmet"), TELMET);
        Registry.register(Registry.ITEM, id("test_chestplate"), TESTPLATE);
        Registry.register(Registry.ITEM, id("test_leggings"), TEGGINGS);
        Registry.register(Registry.ITEM, id("test_boots"), TOOTS);

        registerBlock(id("test_block"), TEST_BLOCK);
        registerBlock(id("test_stairs"), TEST_STAIRS);
        registerBlock(id("test_slab"), TEST_SLAB);
        registerBlock(id("test_door"), TEST_DOOR);
        registerBlock(id("test_trapdoor"), TEST_TRAP_DOOR);
        registerBlock(id("test_block_glowing"), TEST_BLOCK_GLOWING);
        registerBlock(id("test_block_wizard"), TEST_BLOCK_WIZARD);

        Registry.register(Registry.ENCHANTMENT, id("test_enchantment"), new TestEnchantment());

        CommandRegistrationCallback.EVENT.register(TestCommands::register);
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
