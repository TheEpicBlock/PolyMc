package io.github.theepicblock.polymc.impl.mixin;

import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomBlockBreakingCheck {
    /**
     * @param block The block the player is looking at
     * @return True if the player needs to have custom breaking speeds
     */
    public static boolean needsCustomBreaking(ServerPlayerEntity player, Block block) {
        if (!Util.isPolyMapVanillaLike(player) || player.isCreative())
            return false;

        var polyMap = PolyMapProvider.getPolyMap(player);
        return polyMap.getBlockPoly(block) != null || polyMap.getItemPoly(player.getMainHandStack().getItem()) != null;
    }
}
