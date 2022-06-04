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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.impl.misc.PolyDumper;
import io.github.theepicblock.polymc.impl.misc.logging.CommandSourceLogger;
import io.github.theepicblock.polymc.impl.misc.logging.ErrorTrackerWrapper;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.wizard.PacketCountManager;
import io.github.theepicblock.polymc.impl.poly.wizard.ThreadedWizardUpdater;
import io.github.theepicblock.polymc.impl.resource.ResourcePackGenerator;
import io.github.theepicblock.polymc.mixins.TACSAccessor;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
                                    }))
                            .then(literal("getWizardRestrictions")
                                    .executes(context -> doGetWizardRestrictions(context, context.getSource().getPlayer()))
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes(context -> doGetWizardRestrictions(context, EntityArgumentType.getPlayer(context, "player"))))))
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
                                                var pack = PolyMc.getMapForResourceGen().generateResourcePack(logger);
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
                                            } catch (Throwable e) {
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
                                            PolyDumper.dumpPolyMap(PolyMc.getMapForResourceGen(), "PolyDump.txt", logger);
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

    public static int doGetWizardRestrictions(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        if (ConfigManager.getConfig().enableWizardThreading && !ThreadedWizardUpdater.MAIN.isOnThread()) {
            ThreadedWizardUpdater.MAIN.executeSync(() -> doGetWizardRestrictions(context, player));
        }

        var trackerInfo = PacketCountManager.INSTANCE.getTrackerInfoForPlayer(player);
        var source = context.getSource();

        var headerColour = ConfigManager.getConfig().enableWizardThreading ? Formatting.GOLD : Formatting.AQUA;
        source.sendFeedback(new LiteralText("=== Packet restriction info for ").formatted(headerColour)
                .append(player.getDisplayName())
                .append(new LiteralText(" ===").formatted(headerColour)), false);

        var hoverTxt = new LiteralText("Target packet count: ").append(new LiteralText(PacketCountManager.MIN_PACKETS+"-"+PacketCountManager.MAX_PACKETS+" packets per tick").formatted(Formatting.AQUA));
        source.sendFeedback(new LiteralText("Average packet count per tick: ")
                .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTxt)))
                .append(packetCount2Text(trackerInfo.calculateAveragePacketCount())), false);
        var packetHistory = new LiteralText("History: [");
        for (int i = 0; ; i++) {
            packetHistory.append(packetCount2Text(trackerInfo.getPacketHistory()[i]));
            if (i == trackerInfo.getPacketHistory().length-1) {
                source.sendFeedback(packetHistory.append("]"), false);
                break;
            }
            packetHistory.append(", ");
        }

        var restrictionLevelTxt = new LiteralText(String.valueOf(trackerInfo.getRestrictionLevel()));
        if (trackerInfo.getRestrictionLevel() > PacketCountManager.MAX_RESTRICTION) {
            restrictionLevelTxt.formatted(Formatting.DARK_PURPLE);
        } else if (trackerInfo.getRestrictionLevel() > 8) {
            restrictionLevelTxt.formatted(Formatting.RED);
        } else if (trackerInfo.getRestrictionLevel() > 5) {
            restrictionLevelTxt.formatted(Formatting.YELLOW);
        } else {
            restrictionLevelTxt.formatted(Formatting.DARK_GREEN);
        }
        source.sendFeedback(new LiteralText("Restriction level: ").append(restrictionLevelTxt), false);
        var watchDistance = ((TACSAccessor)context.getSource().getWorld().getChunkManager().threadedAnvilChunkStorage).getWatchDistance();
        source.sendFeedback(new LiteralText("Watch distance/radius: ").append(new LiteralText(watchDistance+"/"+PacketCountManager.getWatchRadiusFromDistance(watchDistance)).formatted(Formatting.AQUA)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Text packetCount2Text(int count) {
        var t = new LiteralText(String.valueOf(count));
        if (count > PacketCountManager.MAX_PACKETS * 1.6) {
            t.formatted(Formatting.RED);
        } else if (count > PacketCountManager.MAX_PACKETS) {
            t.formatted(Formatting.YELLOW);
        } else if (count > PacketCountManager.MIN_PACKETS) {
            t.formatted(Formatting.DARK_GREEN);
        } else {
            t.formatted(Formatting.GREEN);
        }
        var hoverText = new LiteralText("")
                .append(t.copy().setStyle(t.getStyle()))
                .append(new LiteralText(" packets per tick =  ").formatted(Formatting.RESET))
                .append(new LiteralText(String.valueOf(count * 20)).setStyle(t.getStyle()))
                .append(new LiteralText(" packets per second").formatted(Formatting.RESET));
        return t.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
    }
}
