package io.github.theepicblock.polymc.impl.misc.logging;

import io.github.theepicblock.polymc.PolyMc;
import org.apache.logging.log4j.Logger;

public class Log4JWrapper implements SimpleLogger {
    private final Logger wrapped;

    public Log4JWrapper(Logger wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void error(String string) {
        wrapped.error(string);
    }

    @Override
    public void warn(String string) {
        wrapped.warn(string);
    }

    @Override
    public void info(String string) {
        wrapped.info(string);
    }
}
