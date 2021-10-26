package net.lamgc.oracle.sentry.common.retry;

/**
 * 断言校验器.
 * <p> 校验器可帮助 {@link Retryer} 检查执行结果是否符合预期, 如果不符合预期, 那么将会重新执行任务.
 * @param <R> 执行结果类型.
 * @author LamGC
 */
@FunctionalInterface
public interface AssertionChecker<R> {

    void check(R result) throws RetryAssertException;

}
