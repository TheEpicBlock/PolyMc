package io.github.theepicblock.polymc.impl.misc.logging;

/**
 * Wrapper for a {@link SimpleLogger} that tracks how many errors have been logged
 */
public class ErrorTrackerWrapper implements SimpleLogger {
    protected final SimpleLogger wrapped;
    public int errors = 0;

    public ErrorTrackerWrapper(SimpleLogger wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void error(String string) {
        wrapped.error(string);
        errors++;
    }

    @Override
    public void warn(String string) {
        wrapped.warn(string);
        errors++;
    }

    @Override
    public void info(String string) {
        wrapped.info(string);
    }
}
