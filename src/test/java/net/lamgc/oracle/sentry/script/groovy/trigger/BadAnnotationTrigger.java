package net.lamgc.oracle.sentry.script.groovy.trigger;

@TriggerName("")
public class BadAnnotationTrigger extends BaseTestTrigger {
    @Override
    public void run(Runnable task) {
        throw new UnsupportedOperationException("Unavailable trigger.");
    }
}
