package io.github.theepicblock.polymc.mixins;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface TACSAccessor {
    @Accessor
    int getWatchDistance();

    @Invoker("method_39975")
    static boolean callIsOnDistanceEdge(int sourceChunkX, int sourceChunkZ, int playerChunkX, int playerChunkZ, int watchDistance) {
        throw new IllegalStateException();
    }
}
