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
import io.github.theepicblock.polymc.api.DebugInfoProvider;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers the polymc commands.
 */
public class PolyMcCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("polymc").requires(source -> source.hasPermissionLevel(2))
                    .then(literal("item")
                            .executes((context) -> {
                                ItemStack heldItem = context.getSource().getPlayer().inventory.getMainHandStack();
                                context.getSource().sendFeedback(PolyMc.getMap().getClientItem(heldItem).toTag(new CompoundTag()).toText(), false);
                                return Command.SINGLE_SUCCESS;
                            }))
                    .then(literal("gen_resource")
                            .executes((context -> {
                                try {
                                    ResourcePackGenerator.generate();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                context.getSource().sendFeedback(new LiteralText("Finished generating"), true);
                                return Command.SINGLE_SUCCESS;
                            })))
                    .then(literal("dump_polyMap")
                            .executes((context) -> {
                                StringBuilder out = new StringBuilder();
                                PolyMap map = PolyMc.getMap();
                                out.append("###########\n###ITEMS###\n###########\n");
                                map.getItemPolys().forEach((item, poly) -> {
                                    addAndFormatToBuilder(out, item, item.getTranslationKey(), poly);
                                });
                                out.append("############\n###BLOCKS###\n############\n");
                                map.getBlockPolys().forEach((block, poly) -> {
                                    addAndFormatToBuilder(out, block, block.getTranslationKey(), poly);
                                });

                                File polyDump = new File(FabricLoader.getInstance().getGameDirectory(), "PolyDump.txt");
                                try {
                                    if (polyDump.exists()) {
                                        boolean a = polyDump.delete();
                                        if (!a) {
                                            throw new SimpleCommandExceptionType(new LiteralText("Failed to remove file so new one could be created")).create();
                                        }
                                    }
                                    boolean b = polyDump.createNewFile();
                                    if (!b) {
                                        throw new SimpleCommandExceptionType(new LiteralText("couldn't create file")).create();
                                    }
                                    FileWriter writer = new FileWriter(polyDump);
                                    writer.write(out.toString());
                                    writer.close();
                                } catch (IOException e) {
                                    context.getSource().sendError(new LiteralText("an error occurred when trying to write the PolyDump. Please check the console"));
                                    e.printStackTrace();
                                }
                                return Command.SINGLE_SUCCESS;
                            })));
        });
    }

    private static <T> void addAndFormatToBuilder(StringBuilder b, T object, String key, DebugInfoProvider<T> poly) {
        b.append(Util.expandTo(key, 45));
        b.append(" --> ");
        b.append(Util.expandTo(poly.getClass().getName(), 60));
        try {
            String info = poly.getDebugInfo(object);
            if (info != null) {
                b.append("|");
                b.append(info);
            }
        } catch (Exception e) {
            PolyMc.LOGGER.debug("Error whilst getting debug info from " + poly.getClass().getName() + " polying " + key);
            e.printStackTrace();
        }
        b.append("\n");
    }
}
