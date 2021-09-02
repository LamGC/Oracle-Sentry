package net.lamgc.oracle.sentry.script.groovy.trigger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import groovy.lang.Closure;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 只执行一次的触发器, 执行后将不再执行该任务.
 * @author LamGC
 */
@TriggerName("once")
public class OnceTrigger implements GroovyTrigger {
    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                    .setNameFormat("GroovyOnceExec-%d")
                    .setUncaughtExceptionHandler((t, e) -> LoggerFactory.getLogger(OnceTrigger.class)
                            .error("脚本执行时发生未捕获异常.", e))
            .build());

    @Override
    public void run(Closure<?> task) {
        EXECUTOR.execute(task);
    }

    @Override
    public void shutdown() {
        // Nothing.
    }
}
