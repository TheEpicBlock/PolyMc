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
package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.resource.ResourcePackGenerator;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers the polymc command
 */
public class PolyMcCommands {
    static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("polymc").requires(source -> source.hasPermissionLevel(2))
                    .then(literal("item")
                            .executes((context) -> {
                                ItemStack heldItem = context.getSource().getPlayer().inventory.getMainHandStack();
                                context.getSource().sendFeedback(PolyMc.getMap().getClientItem(heldItem).toTag(new CompoundTag()).toText(),false);
                                return 0;
                            }))
                    .then(literal("gen_resource")
                            .executes((context -> {
                                ResourcePackGenerator.generate();
                                context.getSource().sendFeedback(new LiteralText("Finished generating"),true);
                                return 0;
                            }))));
        });
    }
}
