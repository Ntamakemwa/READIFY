package com.readify.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersioned {
    String value() default "v1";
    boolean deprecated() default false;
    String sunsetDate() default "";
    String migrationGuide() default "";
}
