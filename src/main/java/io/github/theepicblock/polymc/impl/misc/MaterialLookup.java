package io.github.theepicblock.polymc.impl.misc;

import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;

/**
 * Class to get the original Material of a block
 */
public class MaterialLookup {

    public static Type getMaterial(Block block, BlockState state) {

        if (block instanceof GlassBlock || block instanceof PaneBlock) {
            return Type.GLASS;
        }

        return getMaterial(block.getSoundGroup(state));
    }

    public static Type getMaterial(Block block) {
        return getMaterial(block, block.getDefaultState());
    }

    public static Type getMaterial(BlockSoundGroup soundGroup) {

        if (soundGroup == BlockSoundGroup.METAL) {
            return Type.METAL;
        }

        if (soundGroup == BlockSoundGroup.WOOD) {
            return Type.WOOD;
        }

        if (soundGroup == BlockSoundGroup.STONE) {
            return Type.STONE;
        }

        if (soundGroup == BlockSoundGroup.GRAVEL ||
            soundGroup == BlockSoundGroup.SAND) {
            return Type.SAND;
        }

        if (soundGroup == BlockSoundGroup.WOOL) {
            return Type.WOOL;
        }

        if (soundGroup == BlockSoundGroup.ROOTED_DIRT || soundGroup == BlockSoundGroup.MUD) {
            return Type.DIRT;
        }

        return null;
    }

    public static Type getMaterial(BlockState blockState) {
        return getMaterial(blockState.getBlock(), blockState);
    }

    public static Type getMaterial(DoorBlock block) {
        Type result = getMaterial(block.getBlockSetType());

        if (result == null) {
            result = getMaterial(block.getSoundGroup(block.getDefaultState()));
        }

        return result;
    }

    public static Type getMaterial(BlockSetType blockSetType) {

        Type result = null;

        if (blockSetType == BlockSetType.OAK ||
            blockSetType == BlockSetType.SPRUCE ||
            blockSetType == BlockSetType.BIRCH ||
            blockSetType == BlockSetType.ACACIA ||
            blockSetType == BlockSetType.CHERRY ||
            blockSetType == BlockSetType.JUNGLE ||
            blockSetType == BlockSetType.DARK_OAK ||
            blockSetType == BlockSetType.CRIMSON ||
            blockSetType == BlockSetType.WARPED ||
            blockSetType == BlockSetType.MANGROVE ||
            blockSetType == BlockSetType.BAMBOO) {
            result = Type.WOOD;
        } else if (blockSetType == BlockSetType.STONE ||
                blockSetType == BlockSetType.POLISHED_BLACKSTONE) {
            result = Type.STONE;
        } else if (blockSetType == BlockSetType.IRON ||
                blockSetType == BlockSetType.GOLD) {
            result = Type.METAL;
        }

        return result;
    }

    public enum Type {
        METAL,
        WOOD,
        STONE,
        DIRT,
        SAND,
        GLASS,
        WOOL,
        LEAVES,
        PLANT,
        OTHER
    }
}
