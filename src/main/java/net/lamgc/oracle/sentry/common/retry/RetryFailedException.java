package net.lamgc.oracle.sentry.common.retry;

/**
 * 重试失败异常.
 * <p> 该异常是由于某一个任务尝试执行失败过多而抛出, 失败的原因可能是任务执行时抛出异常, 或执行结果与预期不符.
 */
public final class RetryFailedException extends Exception {

    public RetryFailedException(String message) {
        super(message);
    }

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryFailedException(Throwable cause) {
        super(cause);
    }
}