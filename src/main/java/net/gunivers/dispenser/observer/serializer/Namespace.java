package net.gunivers.dispenser.observer.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Namespace {

    /**
     * @return the root path for this resource
     */
    String value() default "";

}
