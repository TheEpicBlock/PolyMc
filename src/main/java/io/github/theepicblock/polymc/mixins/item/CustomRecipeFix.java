package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.Util;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Minecraft syncs the entire recipe library when the client joins.
 * With mods, this recipe list can contain modded recipe types.
 * This fix prevents those from being sent
 */
@Mixin(SynchronizeRecipesS2CPacket.class)
public class CustomRecipeFix {
    @Inject(method = "writeRecipe(Lnet/minecraft/recipe/Recipe;Lnet/minecraft/network/PacketByteBuf;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static <T extends Recipe<?>> void writeInject(T recipe, PacketByteBuf buf, CallbackInfo ci) {
        Identifier recipeId = Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer());
        if (!Util.isVanilla(recipeId)) {
            ci.cancel();
        }
    }

    @Redirect(method = "write(Lnet/minecraft/network/PacketByteBuf;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public int sizeRedirect(List<Recipe<?>> list) {
        return (int) list.stream().map(recipe -> Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer())).filter(Util::isVanilla).count();
    }
}
