package net.lamgc.oracle.sentry.common.retry;

@FunctionalInterface
public interface RetryExceptionHandler {

    /**
     * 处理异常, 并指示是否继续重试.
     * @param e 异常对象.
     * @return 如果可以继续重试, 返回 {@code true}.
     */
    boolean handle(Exception e);

}
