package net.lamgc.oracle.sentry.common.retry;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.*;

/**
 * 重试器.
 * <p> 通过重试器, 可以对某一个可能失败的任务做重试, 尽可能确保任务执行成功.
 * @param <R> 任务结果类型.
 * @author LamGC
 */
public final class Retryer<R> {


    private final static ThreadPoolExecutor ASYNC_EXECUTOR = new ThreadPoolExecutor(
            1,
            Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() / 2)),
            10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new ThreadFactoryBuilder()
                    .setNameFormat("Thread-Retryer-%d")
                    .build()
    );

    private final RetryableTask<R> task;
    private final int retryNumber;
    private final RetryDelayer delayer;
    private final Set<AssertionChecker<R>> checkers = new HashSet<>();
    private final RetryExceptionHandler handler;

    private Retryer(RetryableTask<R> task, int retryNumber, RetryDelayer delayer, Set<AssertionChecker<R>> checkers, RetryExceptionHandler handler) {
        this.handler = handler;
        if (retryNumber < 0) {
            throw new IllegalArgumentException("The number of retries is not allowed to be negative: " + retryNumber);
        }
        this.task = Objects.requireNonNull(task);
        this.delayer = Objects.requireNonNull(delayer);
        this.retryNumber = retryNumber;
        if (checkers != null && !checkers.isEmpty()) {
            this.checkers.addAll(checkers);
        }
    }

    @SuppressWarnings("BusyWait")
    private R execute0() throws Exception {
        Exception lastException;
        int currentRetryCount = 0;
        do {
            try {
                R result = task.run();
                checkResult(result);
                return result;
            } catch (Exception e) {
                lastException = e;
                if (e instanceof InterruptedException) {
                    break;
                }
                if (handler != null && !handler.handle(e)) {
                    break;
                }
                if (currentRetryCount >= retryNumber) {
                    break;
                }
                long delayTime = delayer.nextDelayTime(currentRetryCount);
                if (delayTime > 0) {
                    try {
                        Thread.sleep(delayTime);
                    } catch (InterruptedException interrupted) {
                        break;
                    }
                }
                currentRetryCount ++;
            }
        } while (true);
        throw lastException;
    }

    /**
     * 使用 {@link AssertionChecker} 检查结果是否符合预期.
     * <p> 当结果不符合检验器预期时, 检验器将抛出 {@link RetryAssertException} 来表示结果不符预期,
     * {@link Retryer} 将会重试该任务.
     * @param result 执行结果.
     * @throws RetryAssertException 当断言检验器对结果断言失败时抛出该异常.
     */
    private void checkResult(R result) throws RetryAssertException {
        for (AssertionChecker<R> checker : checkers) {
            checker.check(result);
        }
    }

    /**
     * 异步执行任务.
     * <p> 使用线程池执行 task.
     * @return 返回 Future 对象以跟踪异步执行结果.
     */
    public Future<R> executeAsync() {
        return ASYNC_EXECUTOR.submit(this::execute0);
    }

    /**
     * 同步执行任务.
     * @return 如果执行完成且成功, 返回执行结果.
     * @throws RetryFailedException 当重试多次仍失败时抛出该异常.
     */
    public R execute() throws RetryFailedException {
        Future<R> future = executeAsync();
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof ExecutionException exception) {
                throw new RetryFailedException("Failure after a certain number of attempts.", exception);
            } else {
                throw new RetryFailedException(e);
            }
        }
    }

    /**
     * 获取一个构建器.
     * @param task 需要重试的任务.
     * @param <R> 结果类型.
     * @return 返回新的构造器.
     */
    public static <R> Builder<R> builder(RetryableTask<R> task) {
        return new Builder<>(task);
    }

    /**
     * {@link Retryer} 构造器.
     * <p> 可通过链式调用快速创建 {@link Retryer}.
     * @param <R> 任务结果类型.
     */
    public static class Builder<R> {

        private final RetryableTask<R> task;
        private RetryDelayer delayer = new FixedTimeDelayer(0);
        private int retryNumber = 0;
        private final Set<AssertionChecker<R>> checkers = new HashSet<>();
        private RetryExceptionHandler handler = (e) -> true;

        private Builder(RetryableTask<R> task) {
            this.task = task;
        }

        public Retryer<R> create() {
            return new Retryer<>(task, retryNumber, delayer, checkers, handler);
        }

        public Builder<R> delayer(RetryDelayer delayer) {
            this.delayer = delayer;
            return this;
        }

        public Builder<R> retryIfReturnNull() {
            this.checkers.add(NonNullChecker.getInstance());
            return this;
        }

        public Builder<R> retryNumber(int number) {
            this.retryNumber = number;
            return this;
        }

        public Builder<R> checker(AssertionChecker<R> checker) {
            checkers.add(checker);
            return this;
        }

        public Builder<R> exceptionHandler(RetryExceptionHandler handler) {
            this.handler = handler == null ? (e) -> true : handler;
            return this;
        }
    }

}
