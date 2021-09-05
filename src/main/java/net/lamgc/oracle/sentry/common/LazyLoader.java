package net.lamgc.oracle.sentry.common;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 惰性加载器.
 * <p> 该加载器只会在第一次获取对象时初始化.
 * <p> 如果不用, 可能会导致内存消耗增加, 尤其是在管理大量帐号的情况下.
 * <p> 对象在本加载器中为单例, 任何通过同一个加载器获取的对象都是同一个对象.
 * @author LamGC
 * @param <T> 对象类型.
 */
public final class LazyLoader<T> {

    private final Supplier<T> supplier;
    private final AtomicReference<T> object = new AtomicReference<>(null);

    /**
     * 构建惰性加载器.
     * @param supplier 对象提供器.
     */
    public LazyLoader(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    /**
     * 获取对象.
     * <p> 如果是首次调用本方法, 本方法将通过 Supplier 获取一个对象, 缓存对象后返回.
     * @return 返回惰性加载的对象.
     */
    public T getInstance() {
        if (object.get() == null) {
            synchronized (this) {
                if (object.get() == null) {
                    T newInstance = supplier.get();
                    if (newInstance == null) {
                        throw new NullPointerException("Supplier is not allowed to return null.");
                    }
                    return object.compareAndSet(null, newInstance) ? newInstance : object.get();
                }
            }
        }
        return object.get();
    }

}