package com.mateolegi.rostrum.annotation;

import java.lang.annotation.*;

/**
 * Indicates that the field will be encrypted before being persisted.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Crypt {

    Type type() default Type.ONE_WAY;
}
