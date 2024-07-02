package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomBlockBreakingCheck {

    /**
     * @param block The block the player is looking at
     * @return True if the player needs to have custom breaking speeds
     */
    public static boolean needsCustomBreaking(ServerPlayerEntity player, Block block) {
        if (player instanceof FakePlayer || !Util.isPolyMapVanillaLike(player) || player.isCreative())
            return false;

        return needsCustomBreaking(player, block.getDefaultState());
    }

    /**
     * @param blockState The blockState the player is looking at
     * @return True if the player needs to have custom breaking speeds
     */
    public static boolean needsCustomBreaking(ServerPlayerEntity player, BlockState blockState) {
        if (player instanceof FakePlayer || !Util.isPolyMapVanillaLike(player) || player.isCreative())
            return false;

        var polyMap = PolyMapProvider.getPolyMap(player);

        // A modded block is being broken, this always requires custom breaking
        if (polyMap.getBlockPoly(blockState.getBlock()) != null) {
            return true;
        }

        // If the modded stack has a ToolComponent, the client one will get it too.
        // This means we might not need any trickery for breaking vanilla blocks.
        var handStack = player.getMainHandStack();
        var handItem = handStack.getItem();

        if (polyMap.getItemPoly(handItem) != null) {
            var toolComponent = handStack.get(DataComponentTypes.TOOL);

            if (toolComponent == null) {
                return true;
            }

            return toolComponent.isCorrectForDrops(blockState);
        }

        return false;
    }
}
