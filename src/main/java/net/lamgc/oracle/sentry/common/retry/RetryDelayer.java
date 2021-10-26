package net.lamgc.oracle.sentry.common.retry;

/**
 * 重试延迟器.
 * <p> 用于决定每次重试的间隔时间, 可根据重试次数调整间隔时常, 以避免频繁执行影响性能.
 * @author LamGC
 */
public interface RetryDelayer {

    /**
     * 获取下一次重试延迟时间.
     * @param currentRetryCount 当前重试次数, 如果第一次重试失败, 则本参数为 0.
     * @return 返回延迟时间.
     */
    long nextDelayTime(int currentRetryCount);

}
