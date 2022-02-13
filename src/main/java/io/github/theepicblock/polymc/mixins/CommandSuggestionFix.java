package io.github.theepicblock.polymc.mixins;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public class CommandSuggestionFix {
    /**
     * Replaces the ItemStack and BlockState argument types with a list of identifiers instead.
     * This list is calculated serverside and will therefore include modded blocks and items.
     *
     * Code from Polymer:
     * https://github.com/Patbox/polymer/blob/e8007afecfb9cadc5bb0fdcca0d8c6bb47fb9a21/src/main/java/eu/pb4/polymer/mixin/command/CommandManagerMixin.java#L31
     */
    @Inject(method = "argument", at = @At("TAIL"), cancellable = true)
    private static void makeSuggestionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<ServerCommandSource, ?>> cir) {
        if (type instanceof ItemStackArgumentType || type instanceof BlockStateArgumentType) {
            cir.setReturnValue(cir.getReturnValue().suggests(type::listSuggestions));
        }
    }
}
