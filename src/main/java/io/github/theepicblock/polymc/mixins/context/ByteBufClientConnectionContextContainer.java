package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.ClientConnectionContextContainer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PacketByteBuf.class)
public class ByteBufClientConnectionContextContainer implements ClientConnectionContextContainer {

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
