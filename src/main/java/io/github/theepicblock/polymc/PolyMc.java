package io.github.theepicblock.polymc;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.register.PolyMapBuilder;
import io.github.theepicblock.polymc.generator.Generator;
import io.github.theepicblock.polymc.resource.ResourceGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.logging.Logger;

import static net.minecraft.server.command.CommandManager.literal;

public class PolyMc implements ModInitializer {
    private static PolyMap map;
    public static final Logger LOGGER = Logger.getLogger("PolyMc");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("polymc_debug").requires(source -> source.hasPermissionLevel(2))
                .then(literal("item")
                    .executes((context) -> {
                        ItemStack heldItem = context.getSource().getPlayer().inventory.getMainHandStack();
                        context.getSource().sendFeedback(new LiteralText(getMap().getClientItem(heldItem).toTag(new CompoundTag()).toString()),false);
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

    /**
     * Builds the poly map, this should only be run when all blocks/items have been registered.
     * This will be called by PolyMc when the worlds are generated.
     * @deprecated this is an internal method you shouldn't call
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void generatePolyMap() {
        PolyMapBuilder builder = new PolyMapBuilder();
        //TODO let other mods generate items here via an entry point

        //Auto generate the rest
        Generator.generateMissing(builder);

        map = builder.build();
    }

    /**
     * Gets the polymap needed to translate from server items to client items.
     * @throws NullPointerException if you try to access it before the server worlds get initialized
     * @return the PolyMap
     */
    public static PolyMap getMap() {
        if (map == null) {
            throw new NullPointerException("Tried to access the PolyMap before it was initialized");
        }
        return map;
    }
}
