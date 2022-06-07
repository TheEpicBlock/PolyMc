package nl.theepicblock.polymc.testmod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.theepicblock.polymc.api.resource.ClientJarResources;
import io.github.theepicblock.polymc.api.resource.json.JBlockStateVariant;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.CommandSourceLogger;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.ClientJarResourcesImpl;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.property.Properties;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class TestCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("polymc-test")
                .then(literal("find-states").executes(TestCommands::findState)));
    }

    private static int findState(CommandContext<ServerCommandSource> ctx) {

        SimpleLogger commandSource = new CommandSourceLogger(ctx.getSource(), true);
        try {
            // Using internal PolyMc api's to access the client jar
            ClientJarResources clientJar = new ClientJarResourcesImpl(commandSource);

            var statesPerVariant = new HashMap<JBlockStateVariant,List<BlockState>>();
            for (var block : Registry.BLOCK) {
                var id = Registry.BLOCK.getId(block);
                var blockStateDefinitions = clientJar.getBlockState(id.getNamespace(), id.getPath());

                if (blockStateDefinitions == null) continue;

                for (var state : block.getStateManager().getStates()) {
                    if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
                        continue;
                    }
                    var stateDef = blockStateDefinitions.getVariantsBestMatching(state);
                    if (stateDef == null || stateDef.length != 1) {
                        // Not going to bother
                        continue;
                    }
                    var list = statesPerVariant.computeIfAbsent(stateDef[0], (a) -> new ArrayList<>());
                    list.add(state);
                }
            }

            for (var states : statesPerVariant.values()) {
                // List all variants that have multiple states
                if (states.size() > 1) {
                    commandSource.info("### "+states.size());
                    states.stream().limit(10).forEach(state -> {
                        commandSource.info(state.getBlock().getTranslationKey() + " : " + Util.getPropertiesFromBlockState(state));
                    });
                }
            }

        } catch (IOException e) {
            commandSource.error("An error occurred! Please check the console.");
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
