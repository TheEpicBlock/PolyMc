package io.github.theepicblock.polymc.impl.misc.logging;

public record SilentLogger() implements SimpleLogger {
    @Override
    public void error(String string) {

    }

    @Override
    public void warn(String string) {

    }

    @Override
    public void info(String string) {

    }
}
