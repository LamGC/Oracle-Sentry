package net.lamgc.oracle.sentry.common.retry;

/**
 * 可重试任务.
 * <p> 实现该方法后, 该任务可在 {@link Retryer} 运行, 可用于一些需重试的任务.
 * @param <R> 执行结果类型.
 */
@FunctionalInterface
public interface RetryableTask<R> {

    /**
     * 运行方法.
     * <p> 当该方法抛出异常, 或经 {@link AssertionChecker} 检查认为结果与预期不符时, 将会被重新运行.
     * <p> 请注意, 即使任务执行完成, 若 {@link AssertionChecker} 认为结果与预期不符, 任务将会被重新运行, 请注意处理该情况.
     * @throws Exception 当异常抛出时, 将视为执行失败, 重试器将根据设定自动重新执行.
     * @return 根据需要可返回.
     */
    R run() throws Exception;

}
