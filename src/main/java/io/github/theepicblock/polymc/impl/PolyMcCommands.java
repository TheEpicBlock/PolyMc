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
package io.github.theepicblock.polymc.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.misc.PolyDumper;
import io.github.theepicblock.polymc.impl.misc.logging.CommandSourceLogger;
import io.github.theepicblock.polymc.impl.misc.logging.ErrorTrackerWrapper;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers the polymc commands.
 */
public class PolyMcCommands {
    private static boolean isGeneratingResources = false;

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("polymc").requires(source -> source.hasPermissionLevel(2))
                    .then(literal("debug")
                            .then(literal("clientItem")
                                    .executes((context) -> {
                                        var player = context.getSource().getPlayer();
                                        var heldItem = player.getInventory().getMainHandStack();
                                        var polydItem = PolyMapProvider.getPolyMap(player).getClientItem(heldItem, player, null);
                                        var heldItemTag = polydItem.writeNbt(new NbtCompound());
                                        var nbtText = NbtHelper.toPrettyPrintedText(heldItemTag);
                                        context.getSource().sendFeedback(nbtText, false);
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("replaceInventoryWithDebug")
                                    .executes((context) -> {
                                        ServerPlayerEntity player = context.getSource().getPlayer();
                                        if (!player.isCreative()) {
                                            throw new SimpleCommandExceptionType(new LiteralText("You must be in creative mode to execute this command. Keep in mind that this will wipe your inventory.")).create();
                                        }
                                        for (int i = 0; i < player.getInventory().size(); i++) {
                                            if (i == 0) {
                                                player.getInventory().setStack(i, new ItemStack(Items.GREEN_STAINED_GLASS_PANE));
                                            } else {
                                                player.getInventory().setStack(i, new ItemStack(Items.RED_STAINED_GLASS_PANE, i));
                                            }
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })))
                    .then(literal("generate")
                            .then(literal("resources")
                                    .executes((context -> {
                                        SimpleLogger commandSource = new CommandSourceLogger(context.getSource(), true);
                                        ErrorTrackerWrapper logger = new ErrorTrackerWrapper(PolyMc.LOGGER);
                                        if (isGeneratingResources) {
                                            commandSource.error("Already generating a resource pack at this moment");
                                            return 0;
                                        }
                                        // Generate pack
                                        isGeneratingResources = true;
                                        new Thread(() -> {
                                            try {
                                                var pack = PolyMc.getMainMap().generateResourcePack(logger);
                                                if (logger.errors != 0) {
                                                    commandSource.error("There have been errors whilst generating the resource pack. These are usually completely normal. It only means that PolyMc couldn't find some of the textures or models. See the console for more info.");
                                                }

                                                // Write pack to file
                                                try {
                                                    ResourcePackGenerator.cleanAndWrite(pack, "resource", logger);

                                                    commandSource.info("Finished generating resource pack");
                                                    commandSource.warn("Before hosting this resource pack, please make sure you have the legal right to redistribute the assets inside.");
                                                } catch (Exception e) {
                                                    commandSource.error("An error occurred whilst trying to save the resource pack! Please check the console.");
                                                    e.printStackTrace();
                                                }
                                            } catch (Exception e) {
                                                commandSource.error("An error occurred whilst trying to generate the resource pack! Please check the console.");
                                                e.printStackTrace();
                                            }

                                            isGeneratingResources = false;
                                        }).start();
                                        commandSource.info("Starting resource generation");
                                        return Command.SINGLE_SUCCESS;
                                    })))
                            .then(literal("polyDump")
                                    .executes((context) -> {
                                        SimpleLogger logger = new CommandSourceLogger(context.getSource(), true);
                                        try {
                                            PolyDumper.dumpPolyMap(PolyMc.getMainMap(), "PolyDump.txt", logger);
                                        } catch (IOException e) {
                                            logger.error(e.getMessage());
                                            return 0;
                                        } catch (Exception e) {
                                            logger.info("An error occurred whilst trying to generate the poly dump! Please check the console.");
                                            e.printStackTrace();
                                            return 0;
                                        }
                                        logger.info("Finished generating poly dump");
                                        return Command.SINGLE_SUCCESS;
                                    }))));
        });
    }
}
