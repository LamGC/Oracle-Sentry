package net.lamgc.oracle.sentry.script.groovy.trigger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 只执行一次的触发器, 执行后将不再执行该任务.
 * @author LamGC
 */
@TriggerName("once")
public class OnceTrigger implements GroovyTrigger {

    private final static ExecutorService executor = Executors.newFixedThreadPool(4,
            new ThreadFactoryBuilder()
                    .setNameFormat("GroovyOnceExec-%d")
            .build());

    @Override
    public void run(Runnable task) {
        executor.execute(task);
    }
}
