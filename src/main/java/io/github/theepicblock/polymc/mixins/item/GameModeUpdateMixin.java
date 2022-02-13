package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.poly.gui.GuiUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin resends the inventory when a player enters creative mode.
 * This is to aid the implementation of {@link io.github.theepicblock.polymc.api.PolyMap#reverseClientItem(ItemStack)}.
 * The javadoc of {@link io.github.theepicblock.polymc.api.PolyMap#reverseClientItem(ItemStack)} states that it may be optimized to only work for items owned by creative mode players.
 * To prevent the situation where the item was owned by a survival player that's now a creative player and the inventory hasn't updated yet, this mixin exists.
 * @see io.github.theepicblock.polymc.impl.PolyMapImpl#recoverOriginalItem(ItemStack)
 * @see PolyMap#getClientItem(ItemStack, ServerPlayerEntity, io.github.theepicblock.polymc.api.item.ItemLocation)
 */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class GameModeUpdateMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "setGameMode(Lnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;)V", at = @At("TAIL"))
    public void onGameModeSet(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        if (gameMode.isCreative()) {
            GuiUtils.resyncPlayerInventory(this.player);
        }
    }
}
