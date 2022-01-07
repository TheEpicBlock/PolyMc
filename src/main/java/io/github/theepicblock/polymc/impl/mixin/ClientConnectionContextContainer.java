package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.network.ClientConnection;

public interface ClientConnectionContextContainer {
    ClientConnection getPolyMcProvidedClientConnection();

    void setPolyMcProvidedClientConnection(ClientConnection connection);

    static ClientConnection retrieve(Object o) {
        return ((ClientConnectionContextContainer)o).getPolyMcProvidedClientConnection();
    }
}
