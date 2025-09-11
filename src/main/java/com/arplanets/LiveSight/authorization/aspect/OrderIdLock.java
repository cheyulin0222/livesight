package com.arplanets.LiveSight.authorization.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderIdLock {

    String value() default "";
}
