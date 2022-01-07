package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.ClientConnectionContextContainer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DecoderHandler.class)
public class DecoderHandlerClientConnectionContextContainer implements ClientConnectionContextContainer {

    @Unique
    private ClientConnection polymc_clientConnection;

    @Override
    public ClientConnection getPolyMcProvidedClientConnection() {
        return this.polymc_clientConnection;
    }

    @Override
    public void setPolyMcProvidedClientConnection(ClientConnection connection) {
        this.polymc_clientConnection = connection;
    }
}
