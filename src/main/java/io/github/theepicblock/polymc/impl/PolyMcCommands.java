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
            dispatcher.register(literal("PolyMc").requires(source -> source.hasPermissionLevel(2))
                    .then(literal("debug")
                        .then(literal("ClientItemNBT")
                            .executes((context) -> {
                                ItemStack heldItem = context.getSource().getPlayer().inventory.getMainHandStack();
                                context.getSource().sendFeedback(PolyMc.getMap().getClientItem(heldItem).toTag(new CompoundTag()).toText(), false);
                                return Command.SINGLE_SUCCESS;
                            })))
                    .then(literal("generate")
                        .then(literal("resources")
                            .executes((context -> {
                                try {
                                    ResourcePackGenerator.generate();
                                } catch (Exception e) {
                                    context.getSource().sendFeedback(new LiteralText("An error occurred whilst trying to generate the resourcepack! Please check the console."), true);
                                    e.printStackTrace();
                                    return 0;
                                }
                                context.getSource().sendFeedback(new LiteralText("Finished generating"), true);
                                return Command.SINGLE_SUCCESS;
                            })))
                        .then(literal("polyDump")
                            .executes((context) -> {
                                StringBuilder polyDump = new StringBuilder();
                                PolyMap map = PolyMc.getMap();
                                polyDump.append("###########\n## ITEMS ##\n###########\n");
                                map.getItemPolys().forEach((item, poly) -> {
                                    addDebugProviderToDump(polyDump, item, item.getTranslationKey(), poly);
                                });
                                polyDump.append("############\n## BLOCKS ##\n############\n");
                                map.getBlockPolys().forEach((block, poly) -> {
                                    addDebugProviderToDump(polyDump, block, block.getTranslationKey(), poly);
                                });

                                File polyDumpFile = new File(FabricLoader.getInstance().getGameDir().toFile(), "PolyDump.txt");
                                try {
                                    if (polyDumpFile.exists()) {
                                        boolean a = polyDumpFile.delete();
                                        if (!a) throw new SimpleCommandExceptionType(new LiteralText("Failed to remove old polyMap")).create();
                                    }
                                    boolean b = polyDumpFile.createNewFile();
                                    if (!b) throw new SimpleCommandExceptionType(new LiteralText("Couldn't create file")).create();

                                    //Write the contents of polyDump to the polyDumpFile
                                    FileWriter writer = new FileWriter(polyDumpFile);
                                    writer.write(polyDump.toString());
                                    writer.close();
                                } catch (IOException e) {
                                    context.getSource().sendError(new LiteralText("An error occurred whilst trying to generate the polyDump! Please check the console."));
                                    e.printStackTrace();
                                    return 0;
                                }
                                return Command.SINGLE_SUCCESS;
                    }))));
        });
    }

    private static <T> void addDebugProviderToDump(StringBuilder b, T object, String key, DebugInfoProvider<T> poly) {
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
            PolyMc.LOGGER.debug(String.format("Error whilst getting debug info from '%s' which is registered to '%s'", poly.getClass().getName(), key));
            e.printStackTrace();
        }
        b.append("\n");
    }
}
