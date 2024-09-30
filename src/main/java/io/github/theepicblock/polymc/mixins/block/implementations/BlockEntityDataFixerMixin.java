package io.github.theepicblock.polymc.mixins.block.implementations;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/packet/s2c/play/ChunkData$BlockEntityData")
public class BlockEntityDataFixerMixin {
    @ModifyExpressionValue(method = "of", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;toInitialChunkDataNbt(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtCompound;"))
    private static NbtCompound changeNbt(NbtCompound original, @Local(argsOnly = true) BlockEntity blockEntity) {
        return Util.transformBlockEntityNbt(PacketContext.get(), blockEntity.getType(), original);
    }
}
