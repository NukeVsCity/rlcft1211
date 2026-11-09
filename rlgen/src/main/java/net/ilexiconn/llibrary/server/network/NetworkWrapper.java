package net.ilexiconn.llibrary.server.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LLibrary 1.7.20 NetworkWrapper: put it on a {@code public static SimpleNetworkWrapper} field and call
 * {@link NetworkHandler#injectNetworkWrapper(Class, String)} for the holder class from the mod constructor (1.12 found the
 * fields with the ASM data table).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NetworkWrapper {
   Class<? extends AbstractMessage<?>>[] value() default {};
}
