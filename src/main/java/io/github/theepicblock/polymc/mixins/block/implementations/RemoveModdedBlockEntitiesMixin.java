package io.github.theepicblock.polymc.mixins.block.implementations;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(ChunkData.class)
public class RemoveModdedBlockEntitiesMixin {
    @WrapWithCondition(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean skipUnsupportedBlockEntities(List<?> instance, Object e, @Local Map.Entry<BlockPos, BlockEntity> entry) {
        var player = ChunkPacketStaticHack.player.get();
        var polyMap = Util.tryGetPolyMap(player);

        return polyMap.canReceiveBlockEntity(entry.getValue().getType());
    }
}
