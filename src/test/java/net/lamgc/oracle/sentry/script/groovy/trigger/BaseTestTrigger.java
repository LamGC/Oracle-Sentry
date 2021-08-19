package net.lamgc.oracle.sentry.script.groovy.trigger;

public abstract class BaseTestTrigger implements GroovyTrigger {
    @Override
    public void run(Runnable task) {
        throw new UnsupportedOperationException("Unavailable trigger.");
    }
}
