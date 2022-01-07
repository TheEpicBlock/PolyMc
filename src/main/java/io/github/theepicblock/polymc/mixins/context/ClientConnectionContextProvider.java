package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientConnection.class)
public class ClientConnectionContextProvider implements PlayerContextContainer {

    @Unique private ServerPlayerEntity player;

    @Override
    public ServerPlayerEntity getPolyMcProvidedPlayer() {
        return player;
    }

    @Override
    public void setPolyMcProvidedPlayer(ServerPlayerEntity v) {
        player = v;
    }
}
