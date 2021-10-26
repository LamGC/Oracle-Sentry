package net.lamgc.oracle.sentry.common.retry;

import java.util.concurrent.TimeUnit;

/**
 * 固定延迟器.
 * <p> 永远以指定的延迟重试.
 * @author LamGC
 */
public final class FixedTimeDelayer implements RetryDelayer {

    private final long delay;

    public FixedTimeDelayer(long time, TimeUnit unit) {
        this(unit.toMillis(time));
    }

    public FixedTimeDelayer(long delay) {
        this.delay = delay;
    }

    @Override
    public long nextDelayTime(int currentRetryCount) {
        return delay;
    }
}
