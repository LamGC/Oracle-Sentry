package net.lamgc.oracle.sentry.common.retry;

/**
 * 非空检查器.
 * <p> 当执行结果为 null 时断言失败.
 * @param <R>
 * @author LamGC
 */
public final class NonNullChecker<R> implements AssertionChecker<R> {

    @SuppressWarnings("rawtypes")
    private final static NonNullChecker INSTANCE = new NonNullChecker();

    @SuppressWarnings("unchecked")
    public static <R> NonNullChecker<R> getInstance() {
        return (NonNullChecker<R>) INSTANCE;
    }

    private NonNullChecker() {}

    @Override
    public void check(Object result) throws RetryAssertException {
        if (result == null) {
            throw new RetryAssertException("The execution result is null.");
        }
    }
}
