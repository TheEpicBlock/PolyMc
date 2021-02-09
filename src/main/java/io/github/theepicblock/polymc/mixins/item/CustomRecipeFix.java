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
import io.github.theepicblock.polymc.impl.mixin.PlayerContextContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Minecraft syncs the entire recipe library when the client joins.
 * With mods, this recipe list can contain modded recipe types.
 * This fix prevents those from being sent
 */
@Mixin(SynchronizeRecipesS2CPacket.class)
public class CustomRecipeFix implements PlayerContextContainer {
    @Unique
    private ServerPlayerEntity player;

    @Override
    public ServerPlayerEntity getPolyMcProvidedPlayer() {
        return player;
    }

    @Override
    public void setPolyMcProvidedPlayer(ServerPlayerEntity v) {
        player = v;
    }

    @Shadow
    private List<Recipe<?>> recipes;

    @Inject(method = "write", at = @At("HEAD"))
    public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
        if (Util.isPolyMapVanillaLike(player)) {
            recipes = recipes.stream()
                    .filter(recipe -> Util.isVanilla(Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer())))
                    .collect(Collectors.toList());
        }
    }
}
