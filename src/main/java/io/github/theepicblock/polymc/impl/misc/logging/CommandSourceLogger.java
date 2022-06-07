package io.github.theepicblock.polymc.impl.misc.logging;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Sends logs to a {@link CommandSourceLogger}
 */
public class CommandSourceLogger implements SimpleLogger {
    protected final ServerCommandSource commandSource;
    protected final boolean sendToOps;

    public CommandSourceLogger(ServerCommandSource commandSource, boolean sendToOps) {
        this.commandSource = commandSource;
        this.sendToOps = sendToOps;
    }

    @Override
    public void error(String string) {
        commandSource.sendFeedback(Text.literal(string).formatted(Formatting.RED), sendToOps);
    }

    @Override
    public void warn(String string) {
        commandSource.sendFeedback(Text.literal(string).formatted(Formatting.YELLOW), sendToOps);
    }

    @Override
    public void info(String string) {
        commandSource.sendFeedback(Text.literal(string), sendToOps);
    }
}
