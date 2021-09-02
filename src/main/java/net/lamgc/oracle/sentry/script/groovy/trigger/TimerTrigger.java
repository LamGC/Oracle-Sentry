package net.lamgc.oracle.sentry.script.groovy.trigger;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;

/**
 * @author LamGC
 */
@SuppressWarnings("unused")
@TriggerName("timer")
public class TimerTrigger implements GroovyTrigger {

    private final static ThreadPoolTaskScheduler SCHEDULER = new ThreadPoolTaskScheduler();
    static {
        SCHEDULER.setPoolSize(Runtime.getRuntime().availableProcessors());
        SCHEDULER.setThreadFactory(new ThreadFactoryBuilder()
                .setNameFormat("Groovy-TimerTrigger-%d")
                .build());
        SCHEDULER.setErrorHandler(t -> getLog().error("脚本执行时发生异常.", t));
        SCHEDULER.initialize();
    }

    private CronTrigger trigger;
    private ScheduledFuture<?> future;

    /**
     * 设定定时时间.
     * <p> 只允许在第一次执行时设置.
     * @param expression Cron 时间表达式.
     */
    public void time(String expression) {
        if (trigger == null) {
            trigger = new CronTrigger(expression);
        }
    }

    @Override
    public synchronized void run(Closure<?> runnable) {
        if (future != null) {
            getLog().warn("脚本存在多个 run 代码块, 已忽略.");
            return;
        }

        if (trigger == null) {
            if (!getLog().isDebugEnabled()) {
                getLog().warn("脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).");
            } else {
                getLog().warn("{} - 脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).", this);
                getLog().warn("{} - 脚本尚未设置 Cron 时间表达式, 任务将不会执行.\n{}", this, Throwables.getStackTraceAsString(new Exception()));
            }
            return;
        } else if (runnable == null) {
            if (!getLog().isDebugEnabled()) {
                getLog().warn("脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).");
            } else {
                getLog().warn("{} - 脚本尚未设置任务动作, 任务将不会执行(堆栈信息请检查调试级别日志).", this);
                getLog().warn("{} - 脚本尚未设置任务动作, 任务将不会执行.\n{}", this, Throwables.getStackTraceAsString(new Exception()));
            }
            return;
        }

        this.future = SCHEDULER.schedule(new TimerTaskRunnable(runnable), trigger);
    }

    @Override
    public void shutdown() {
        if (this.future != null) {
            future.cancel(false);
        }
    }
    
    private static Logger getLog() {
        return LoggerFactory.getLogger(TimerTrigger.class);
    }

    private static class TimerTaskRunnable implements Runnable {

        private final Closure<?> closure;

        private TimerTaskRunnable(Closure<?> closure) {
            this.closure = closure;
        }

        @Override
        public void run() {
            closure.call();
        }
    }

}
