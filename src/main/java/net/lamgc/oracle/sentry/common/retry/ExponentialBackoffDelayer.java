package net.lamgc.oracle.sentry.common.retry;

/**
 * 指数退避延迟器.
 * @author LamGC
 */
public final class ExponentialBackoffDelayer implements RetryDelayer {

    @Override
    public long nextDelayTime(int currentRetryCount) {
        return (4L << currentRetryCount) * 1000;
    }

}
