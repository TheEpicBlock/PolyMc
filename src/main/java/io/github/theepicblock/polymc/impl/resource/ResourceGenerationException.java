package io.github.theepicblock.polymc.impl.resource;

public class ResourceGenerationException extends RuntimeException {
    public ResourceGenerationException(String msg) {
        super(msg);
    }

    public ResourceGenerationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ResourceGenerationException(Throwable cause) {
        super(cause);
    }
}
