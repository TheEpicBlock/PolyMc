package io.github.theepicblock.polymc.impl.misc.logging;

public interface SimpleLogger {
    void error(String string);
    default void error(Object obj) {
        error(obj.toString());
    }

    void warn(String string);
    default void warn(Object obj) {
        warn(obj.toString());
    }

    void info(String string);
    default void info(Object obj) {
        info(obj.toString());
    }
}
