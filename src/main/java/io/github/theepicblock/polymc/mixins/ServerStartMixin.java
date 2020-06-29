package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerStartMixin {
    @Inject(method = "createWorlds(Lnet/minecraft/server/WorldGenerationProgressListener;)V", at = @At("HEAD"))
    public void createWorlds(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        PolyMc.generatePolyMap();
    }
}
