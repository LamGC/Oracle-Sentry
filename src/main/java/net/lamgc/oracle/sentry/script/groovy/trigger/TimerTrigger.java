package net.lamgc.oracle.sentry.script.groovy.trigger;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

/**
 * @author LamGC
 */
@SuppressWarnings("unused")
@TriggerName("timer")
public class TimerTrigger implements GroovyTrigger {

    private final static Logger log = LoggerFactory.getLogger(TimerTrigger.class);

    private CronTrigger trigger;
    private final static ThreadPoolTaskScheduler SCHEDULER = new ThreadPoolTaskScheduler();
    static {
        SCHEDULER.setPoolSize(Runtime.getRuntime().availableProcessors());
        SCHEDULER.setErrorHandler(t -> log.error("脚本执行时发生异常.", t));
    }

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
    public void run(Runnable runnable) {
        if (trigger == null) {
            if (!log.isDebugEnabled()) {
                log.warn("脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).");
            } else {
                log.warn("{} - 脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).", this);
                log.warn("{} - 脚本尚未设置 Cron 时间表达式, 任务将不会执行.\n{}", this, Throwables.getStackTraceAsString(new Exception()));
            }
            return;
        } else if (runnable == null) {
            if (!log.isDebugEnabled()) {
                log.warn("脚本尚未设置 Cron 时间表达式, 任务将不会执行(堆栈信息请检查调试级别日志).");
            } else {
                log.warn("{} - 脚本尚未设置任务动作, 任务将不会执行(堆栈信息请检查调试级别日志).", this);
                log.warn("{} - 脚本尚未设置任务动作, 任务将不会执行.\n{}", this, Throwables.getStackTraceAsString(new Exception()));
            }
            return;
        }

        SCHEDULER.schedule(runnable, trigger);
    }

}
