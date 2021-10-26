package net.lamgc.oracle.sentry.common.retry;

/**
 * 重试断言异常.
 * <p> 当指定条件失败时抛出该异常.
 * <p> 跑出该异常的原因并非任务执行失败, 而是任务执行的结果与预期不符.
 * @author LamGC
 */
public class RetryAssertException extends Exception {

    public RetryAssertException(String message) {
        super(message);
    }

    public RetryAssertException(String message, Throwable cause) {
        super(message, cause);
    }
}