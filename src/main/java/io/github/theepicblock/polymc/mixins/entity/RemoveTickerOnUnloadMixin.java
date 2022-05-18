package io.github.theepicblock.polymc.mixins.entity;

import io.github.theepicblock.polymc.impl.mixin.EntityTrackerEntryDuck;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public class RemoveTickerOnUnloadMixin {
    @Shadow @Final private Int2ObjectMap<ThreadedAnvilChunkStorage.EntityTracker> entityTrackers;

    @Shadow @Final ServerWorld world;

    @Inject(method = "loadEntity(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;"))
    private void onLoadEntity(Entity entity, CallbackInfo ci) {
        var tracker = this.entityTrackers.get(entity.getId());
        ((EntityTrackerEntryDuck)((EntityTrackerAccessor)tracker).getEntry()).polymc$getWizards().forEach((polyMap, wizard) -> {
            ((WizardTickerDuck)this.world).polymc$removeEntityTicker(polyMap, wizard);
        });
    }
}
