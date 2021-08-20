package net.lamgc.oracle.sentry.script.groovy.trigger;

import groovy.lang.Closure;

/**
 * Groovy 脚本的触发器接口.
 * <p> 实现该接口并添加 {@link TriggerName} 注解后,
 * 添加到 SPI 实现列表, 即可作为一个 Trigger.
 * @author LamGC
 */
public interface GroovyTrigger {

    /**
     * 启动触发器.
     * <p> 注意, 触发器执行 run 方法不可以阻塞方法返回.
     * @param task 触发器需要执行的任务.
     */
    void run(Closure<?> task);

}
