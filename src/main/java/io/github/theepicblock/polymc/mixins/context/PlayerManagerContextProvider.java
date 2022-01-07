package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerContextProvider {

    /**
     * Attach the ServerPlayerEntity to the ClientConnection
     */
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void attachPlayerOnConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ((PlayerContextContainer) connection).setPolyMcProvidedPlayer(player);
    }
}
