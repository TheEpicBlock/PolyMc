package io.github.theepicblock.polymc.common;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum BlockPlacementBehaviour {
    /**
     * Block always places a full-block
     */
    FULL_BLOCK(i -> normalBlockItem(i) && allShapes(i, Block::isShapeFullCube) && getPlaceAtClass(i) == AbstractBlock.class && hasNormalCanReplace(i)),
    /**
     * Block always places an empty block
     */
    EMPTY_BLOCK(i -> (normalBlockItem(i) || i.getClass() == TallBlockItem.class) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == AbstractBlock.class && hasNormalCanReplace(i)),
    /**
     * Placeable on farmland
     */
    CROP(i -> (normalBlockItem(i) || i.getClass() == TallBlockItem.class) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == CropBlock.class && getPlantOnTopClass(i) == CropBlock.class && hasNormalCanReplace(i)),
    /**
     * Placeable on farmland and dirt
     */
    PLANT(i -> normalBlockItem(i) && allShapes(i, VoxelShape::isEmpty) && getPlaceAtClass(i) == PlantBlock.class && getPlantOnTopClass(i) == PlantBlock.class && hasNormalCanReplace(i)),
    DOOR(i -> i.getClass() == TallBlockItem.class && getBehaviourClass(i) == DoorBlock.class && getPlaceAtClass(i) == DoorBlock.class && getCollisionClass(i) == DoorBlock.class && hasNormalCanReplace(i)),
    TRAP_DOOR(i -> normalBlockItem(i) && getBehaviourClass(i) == TrapdoorBlock.class && getPlaceAtClass(i) == AbstractBlock.class && getCollisionClass(i) == TrapdoorBlock.class && hasNormalCanReplace(i)),
    SLAB(i -> normalBlockItem(i) && getBehaviourClass(i) == SlabBlock.class && getPlaceAtClass(i) == AbstractBlock.class && getCollisionClass(i) == SlabBlock.class && getCanReplaceClass(i) == SlabBlock.class),
    STAIR(i -> normalBlockItem(i) && getBehaviourClass(i) == StairsBlock.class && getPlaceAtClass(i) == AbstractBlock.class && getCollisionClass(i) == StairsBlock.class && hasNormalCanReplace(i));

    final Predicate<BlockItem> match;

    BlockPlacementBehaviour(Predicate<BlockItem> match) {
        this.match = match;
    }

    @Nullable
    public static BlockPlacementBehaviour get(BlockItem item) {
        for (var behaviour : BlockPlacementBehaviour.values()) {
            if (behaviour.match.test(item)) {
                return behaviour;
            }
        }
        return null;
    }

    private static boolean allShapes(BlockItem i, Predicate<VoxelShape> predicate) {
        // We assume that the block only places states of itself
        var blocks = new HashMap<Block, Item>();
        i.appendBlocks(blocks, i);
        Stream<BlockState> states = blocks.keySet().stream().flatMap(
                block -> block.getStateManager().getStates().stream()
        );

        return states.allMatch(state -> {
            try {
                return predicate.test(state.getCollisionShape(null, null));
            } catch (Exception e) {
                return false;
            }
        });
    }

    private static boolean normalBlockItem(BlockItem item) {
        var itemClass = item.getClass();
        return itemClass == BlockItem.class || itemClass == AliasedBlockItem.class || itemClass == VerticallyAttachableBlockItem.class;
    }

    private static boolean hasNormalCanReplace(BlockItem item) {
        return getCanReplaceClass(item) == AbstractBlock.class || getCanReplaceClass(item) == AbstractPlantBlock.class;
    }

    private static Class<?> getCanReplaceClass(BlockItem item) {
        return getDefiner(item, "canReplace", BlockState.class, ItemPlacementContext.class);
    }

    private static Class<?> getPlantOnTopClass(BlockItem item) {
        return getDefiner(item, "canPlantOnTop", BlockState.class, BlockView.class, BlockPos.class);
    }

    private static Class<?> getPlaceAtClass(BlockItem item) {
        return getDefiner(item, "canPlaceAt", BlockState.class, WorldView.class, BlockPos.class);
    }

    private static Class<?> getBehaviourClass(BlockItem item) {
        return getDefiner(item, "getPlacementState", ItemPlacementContext.class);
    }

    private static Class<?> getDefiner(BlockItem item, String methodName, Class<?>... parameters) {
        var block = item.getBlock();
        var blockClass = block.getClass();
        try {
            var method = blockClass.getMethod(methodName, parameters);
            return method.getDeclaringClass();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Class<?> getCollisionClass(BlockItem item) {
        var block = item.getBlock();
        var blockClass = block.getClass();
        try {
            var method = blockClass.getMethod("getCollisionShape", BlockState.class, BlockView.class, BlockPos.class, ShapeContext.class);
            if (method.getDeclaringClass() == AbstractBlock.class) {
                return blockClass.getMethod("getOutlineShape", BlockState.class, BlockView.class, BlockPos.class, ShapeContext.class).getDeclaringClass();
            } else {
                return method.getDeclaringClass();
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
