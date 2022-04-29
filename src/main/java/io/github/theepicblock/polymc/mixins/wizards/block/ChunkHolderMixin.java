package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.impl.misc.WatchListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin {
    @Shadow public abstract WorldChunk getWorldChunk();

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void onLevelSet(int level, CallbackInfo ci) {
        if (level > ThreadedAnvilChunkStorage.MAX_LEVEL) {
            WorldChunk chunk = this.getWorldChunk();
            if (chunk != null) ((WatchListener)chunk).polymc$removeAllPlayers();
        }
    }
}
