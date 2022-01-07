package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.ClientConnectionContextContainer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/server/ServerNetworkIo$1")
public class ServerNetworkIoContextProvider {

    @Unique
    private DecoderHandler polymc_decoderHandler = null;

    @Redirect(
            method = "initChannel",
            at = @At(value = "INVOKE", target = "io/netty/channel/ChannelPipeline.addLast (Ljava/lang/String;Lio/netty/channel/ChannelHandler;)Lio/netty/channel/ChannelPipeline;")
    )
    public ChannelPipeline modifyDecoder(ChannelPipeline instance, String s, ChannelHandler channelHandler) {

        // First the "decoder" is attached to the pipeline. Remember the decoder.
        if (s.equals("decoder")) {
            this.polymc_decoderHandler = (DecoderHandler) channelHandler;
        } else if (s.equals("packet_handler")) {
            // Later, the "packet_handler" is attached to the pipeline. Which is actually the ClientConnection instance.
            // Attach it to the decoder.
            // This way we can get to the ClientConnection (and thus the ServerPlayerEntity) from the decoder.
            ((ClientConnectionContextContainer) this.polymc_decoderHandler).setPolyMcProvidedClientConnection((ClientConnection) channelHandler);

            // Forget the decoder again
            this.polymc_decoderHandler = null;
        }

        return instance.addLast(s, channelHandler);
    }
}
