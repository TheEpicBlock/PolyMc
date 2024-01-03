package io.github.theepicblock.polymc.mixins.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.poly.item.PlaceableItemPoly;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public abstract class PlaceAnimationAndSoundFix extends Item {
    public PlaceAnimationAndSoundFix(Settings settings) {
        super(settings);
    }

    @ModifyArg(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private PlayerEntity removeSource(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayerEntity
                && Util.tryGetPolyMap(serverPlayerEntity).getItemPoly(this) instanceof PlaceableItemPoly) {
            return null;
        }

        return player;
    }


    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"))
    private ActionResult changeResult(boolean swingHand, Operation<ActionResult> original, @Local(ordinal = 0) ItemPlacementContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayerEntity
                && Util.tryGetPolyMap(serverPlayerEntity).getItemPoly(this) instanceof PlaceableItemPoly) {
            return ActionResult.SUCCESS;
        }

        return original.call(swingHand);
    }
}
