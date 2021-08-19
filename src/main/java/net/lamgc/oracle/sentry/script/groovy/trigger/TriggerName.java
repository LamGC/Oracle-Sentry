package net.lamgc.oracle.sentry.script.groovy.trigger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 触发器名称.
 * @author LamGC
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TriggerName {

    /**
     * Trigger 名称.
     * <p> 需保证唯一性.
     * @return 返回 Trigger 名称.
     */
    String value();

}
