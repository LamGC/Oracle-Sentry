package net.lamgc.oracle.sentry.script.groovy.trigger;

import groovy.lang.Closure;

public abstract class BaseTestTrigger implements GroovyTrigger {
    @Override
    public void run(Closure<?> task) {
        throw new UnsupportedOperationException("Unavailable trigger.");
    }

    @Override
    public void shutdown() {
        // Do nothing.
    }
}
