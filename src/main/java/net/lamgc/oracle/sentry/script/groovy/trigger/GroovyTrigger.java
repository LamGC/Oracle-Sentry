package net.lamgc.oracle.sentry.script.groovy.trigger;

/**
 * @author LamGC
 */
public interface GroovyTrigger {

    /**
     * 启动触发器.
     * <p> 注意, 触发器执行 run 方法不可以阻塞方法返回.
     * @param task 触发器需要执行的任务.
     */
    void run(Runnable task);

}
