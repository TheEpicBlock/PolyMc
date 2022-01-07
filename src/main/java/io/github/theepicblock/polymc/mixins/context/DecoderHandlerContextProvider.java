package io.github.theepicblock.polymc.mixins.context;

import io.github.theepicblock.polymc.impl.mixin.ClientConnectionContextContainer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DecoderHandler.class)
public class DecoderHandlerContextProvider {

    /**
     * Attach the ClientConnection instance to each incoming PacketByteBuf
     */
    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readVarInt()I"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyPacketByteBuf(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objects, CallbackInfo ci, int i, PacketByteBuf packetByteBuf) {

        ClientConnection connection = ((ClientConnectionContextContainer) (DecoderHandler) (Object) this).getPolyMcProvidedClientConnection();

        if (connection != null) {
            ((ClientConnectionContextContainer) packetByteBuf).setPolyMcProvidedClientConnection(connection);
        }
    }
}
