/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.mixins.item;

import io.github.theepicblock.polymc.impl.Util;
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
