package io.github.theepicblock.polymc.impl.generator.asm;

import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this string represents an internal name (eg, one with slashes)
 * @see Type#getInternalName()
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE_USE})
public @interface InternalName {
}
