package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.impl.poly.gui.GuiUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
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
 * @see io.github.theepicblock.polymc.impl.PolyMapImpl#getClientItem(ItemStack, ServerPlayerEntity)
 */
@Mixin(ServerPlayerEntity.class)
public abstract class GameModeUpdateMixin {
    @Shadow public abstract boolean isCreative();

    @Inject(method = "setGameMode", at = @At("TAIL"))
    public void onGameModeSet(GameMode gameMode, CallbackInfo ci) {
        if (this.isCreative()) {
            GuiUtils.resyncPlayerInventory((ServerPlayerEntity)(Object)this);
        }
    }
}
