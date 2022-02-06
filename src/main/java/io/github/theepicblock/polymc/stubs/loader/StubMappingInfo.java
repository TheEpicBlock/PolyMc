package io.github.theepicblock.polymc.stubs.loader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StubMappingInfo {
    /**
     * Name of the target in intermediary
     */
    String intermediary();
}
