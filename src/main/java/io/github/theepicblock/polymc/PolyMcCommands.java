package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.resource.ResourceGenerator;
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
                                ResourceGenerator.generate();
                                context.getSource().sendFeedback(new LiteralText("Finished generating"),true);
                                return 0;
                            }))));
        });
    }
}
