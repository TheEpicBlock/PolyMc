package io.github.theepicblock.polymc.impl.resource;

public class ResourceSaveException extends RuntimeException {
    public ResourceSaveException(String msg) {
        super(msg);
    }

    public ResourceSaveException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ResourceSaveException(Throwable cause) {
        super(cause);
    }
}
