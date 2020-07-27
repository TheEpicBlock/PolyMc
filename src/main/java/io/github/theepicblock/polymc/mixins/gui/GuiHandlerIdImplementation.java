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
package io.github.theepicblock.polymc.mixins.gui;

import io.github.theepicblock.polymc.PolyMc;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OpenScreenS2CPacket.class)
public class GuiHandlerIdImplementation {
    @Redirect(method = "<init>(ILnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/text/Text;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getRawId(Ljava/lang/Object;)I"))
    public <T> int handlerId(Registry<T> registry, T entry) {
        if (entry instanceof ScreenHandlerType<?>) {
            //noinspection unchecked
            return registry.getRawId((T) PolyMc.getMap().getGuiPoly((ScreenHandlerType<?>)entry).getClientSideType());
        }
        return registry.getRawId(entry);
    }
}
