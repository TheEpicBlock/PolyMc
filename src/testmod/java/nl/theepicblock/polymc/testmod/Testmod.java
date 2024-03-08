package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public class Testmod implements ModInitializer {
    private static final String MODID = "polymc-testmod";

    public static final BlockSetType TEST_IRON_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(BlockSoundGroup.BONE)
            .openableByHand(false)
            .register(id("test_iron"));
    public static final BlockSetType TEST_WOOD_BLOCKSET = new BlockSetTypeBuilder()
            .soundGroup(BlockSoundGroup.WOOL)
            .openableByHand(true)
            .register(id("test_wood"));

    public static final Item TEST_ITEM = new TestItem(new Item.Settings().maxCount(6).rarity(Rarity.EPIC));
    public static final Item TEST_FOOD = new Item(new Item.Settings().food(FoodComponents.COOKED_CHICKEN));
    public static final RegistryEntry<ArmorMaterial> TEST_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, id("armor_material"), new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 1);
        map.put(ArmorItem.Type.LEGGINGS, 2);
        map.put(ArmorItem.Type.CHESTPLATE, 3);
        map.put(ArmorItem.Type.HELMET, 1);
        map.put(ArmorItem.Type.BODY, 3);
    }), 5, SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, Ingredient::empty, List.of(new ArmorMaterial.Layer(id("armor_material"))), 1, 1));
    public static final Item TELMET = new ArmorItem(TEST_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings());
    public static final Item TESTPLATE = new ArmorItem(TEST_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings());
    public static final Item TEGGINGS = new ArmorItem(TEST_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings());
    public static final Item TOOTS = new ArmorItem(TEST_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings());

    public static final Block TEST_BLOCK = new TestBlock(FabricBlockSettings.create());
    public static final Block TEST_STAIRS = new TestStairsBlock(TEST_BLOCK.getDefaultState(), FabricBlockSettings.create());
    public static final Block TEST_SLAB = new TestSlabBlock(FabricBlockSettings.create());
    public static final Block TEST_DOOR = new DoorBlock(TEST_WOOD_BLOCKSET, FabricBlockSettings.copyOf(Blocks.OAK_DOOR));
    public static final Block TEST_IRON_DOOR = new DoorBlock(TEST_IRON_BLOCKSET, FabricBlockSettings.copyOf(Blocks.OAK_DOOR));
    public static final Block TEST_TRAP_DOOR = new TrapdoorBlock(TEST_WOOD_BLOCKSET, FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    public static final Block TEST_IRON_TRAP_DOOR = new TrapdoorBlock(TEST_IRON_BLOCKSET, FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    public static final Block TEST_BLOCK_GLOWING = new Block(FabricBlockSettings.create().luminance(9));
    public static final Block TEST_BLOCK_WIZARD = new ColoredFallingBlock(new ColorCode(0), FabricBlockSettings.create());

    public static final EntityType<? extends LivingEntity> TEST_ENTITY_DIRECT = FabricEntityTypeBuilder.create().entityFactory(CreeperEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_DIRECT = FabricEntityTypeBuilder.create().entityFactory(TestExtendDirectEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_MOB = FabricEntityTypeBuilder.create().entityFactory(TestExtendMobEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_EXTEND_GOLEM = FabricEntityTypeBuilder.create().entityFactory(TestExtendGolemEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<? extends LivingEntity> TEST_ENTITY_LIVING = FabricEntityTypeBuilder.create().entityFactory(TestLivingEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<?> TEST_ENTITY_OTHER = FabricEntityTypeBuilder.create().entityFactory(TestOtherEntity::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();
    public static final EntityType<?> TEST_FLYING_WAXED_WEATHERED_CUT_COPPER_STAIRS_ENTITY = FabricEntityTypeBuilder.create().entityFactory(TestFlyingWaxedWeatheredCutCopperStairs::new).trackRangeChunks(4).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build();

    public static final RegistryEntry<StatusEffect> TEST_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, id("yellow_effect"), new YellowStatusEffect(StatusEffectCategory.HARMFUL, 0xf4e42c));
    public static final Potion TEST_POTION_TYPE = Registry.register(Registries.POTION, id("yellow_potion"), new Potion(new StatusEffectInstance(TEST_EFFECT, 9600)));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, id("test_item"), TEST_ITEM);
        Registry.register(Registries.ITEM, id("test_food"), TEST_FOOD);

        Registry.register(Registries.ITEM, id("test_helmet"), TELMET);
        Registry.register(Registries.ITEM, id("test_chestplate"), TESTPLATE);
        Registry.register(Registries.ITEM, id("test_leggings"), TEGGINGS);
        Registry.register(Registries.ITEM, id("test_boots"), TOOTS);

        registerBlock(id("test_block"), TEST_BLOCK);
        registerBlock(id("test_stairs"), TEST_STAIRS);
        registerBlock(id("test_slab"), TEST_SLAB);
        registerBlock(id("test_door"), TEST_DOOR);
        registerBlock(id("test_iron_door"), TEST_IRON_DOOR);
        registerBlock(id("test_trapdoor"), TEST_TRAP_DOOR);
        registerBlock(id("test_iron_trapdoor"), TEST_IRON_TRAP_DOOR);
        registerBlock(id("test_block_glowing"), TEST_BLOCK_GLOWING);
        registerBlock(id("test_block_wizard"), TEST_BLOCK_WIZARD);

        Registry.register(Registries.ENTITY_TYPE, id("test_entity_direct"), TEST_ENTITY_DIRECT);
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_DIRECT, CreeperEntity.createCreeperAttributes());
        Registry.register(Registries.ENTITY_TYPE, id("test_entity_extend_direct"), TEST_ENTITY_EXTEND_DIRECT);
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_DIRECT, CreeperEntity.createCreeperAttributes());

        Registry.register(Registries.ENTITY_TYPE, id("test_entity_extend_mob"), TEST_ENTITY_EXTEND_MOB);
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_MOB, MobEntity.createMobAttributes());

        Registry.register(Registries.ENTITY_TYPE, id("test_entity_extend_golem"), TEST_ENTITY_EXTEND_GOLEM);
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_EXTEND_GOLEM, MobEntity.createMobAttributes());

        Registry.register(Registries.ENTITY_TYPE, id("test_entity_living"), TEST_ENTITY_LIVING);
        FabricDefaultAttributeRegistry.register(TEST_ENTITY_LIVING, LivingEntity.createLivingAttributes());
        Registry.register(Registries.ENTITY_TYPE, id("test_entity_other"), TEST_ENTITY_OTHER);
        Registry.register(Registries.ENTITY_TYPE, id("test_flying_waxed_weathered_cut_copper_stairs_entity"), TEST_FLYING_WAXED_WEATHERED_CUT_COPPER_STAIRS_ENTITY);

        Registry.register(Registries.ENCHANTMENT, id("test_enchantment"), new TestEnchantment());

        CommandRegistrationCallback.EVENT.register(TestCommands::register);

        var e = Registries.POTION.getRawId(TEST_POTION_TYPE);
        System.out.println("qwertgyuwgdyuyqw "+e);
        e = Registries.STATUS_EFFECT.getRawId(TEST_EFFECT.value());
        System.out.println("eeeeeeeeeeeeeeee "+e);
    }

    public static void debugSend(@Nullable PlayerEntity playerEntity, String text) {
        if (playerEntity != null) playerEntity.sendMessage(Text.literal(text), false);
    }

    private static void registerBlock(Identifier id, Block block) {
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
    }

    private static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
