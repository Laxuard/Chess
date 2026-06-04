package com.ft_transcendence.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automated execution latency metrics logging.
 * Automatically outputs execution duration along with active trace ID attributes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    /**
     * Optional description of the action being monitored.
     */
    String value() default "";
}
