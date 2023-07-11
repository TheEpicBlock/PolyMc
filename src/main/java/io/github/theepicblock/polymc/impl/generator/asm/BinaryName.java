package io.github.theepicblock.polymc.impl.generator.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this string represents a binary name (eg, one with dots)
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE_USE})
public @interface BinaryName {
}
