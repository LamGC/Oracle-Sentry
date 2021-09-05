package net.lamgc.oracle.sentry.oci.account;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.core.BlockstorageClient;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import net.lamgc.oracle.sentry.common.LazyLoader;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 甲骨文 SDK 客户端.
 * @author LamGC
 */
public class OracleClients {

    private final Map<Class<?>, LazyLoader<?>> LAZY_LOADER_MAP = new ConcurrentHashMap<>();
    private final AuthenticationDetailsProvider provider;

    OracleClients(AuthenticationDetailsProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        initialLazyLoad();
    }

    private void initialLazyLoad() {
        registryLazyLoader(ComputeClient.class, () -> new ComputeClient(provider));
        registryLazyLoader(VirtualNetworkClient.class, () -> new VirtualNetworkClient(provider));
        registryLazyLoader(BlockstorageClient.class, () -> new BlockstorageClient(provider));
        registryLazyLoader(IdentityClient.class, () -> new IdentityClient(provider));
        registryLazyLoader(ObjectStorageClient.class, () -> new ObjectStorageClient(provider));
    }

    /**
     * 获取计算类客户端.
     * @return 获取计算类客户端对象.
     */
    public ComputeClient compute() {
        return getInstance(ComputeClient.class);
    }

    /**
     * 获取网络客户端.
     * @return 返回 VCN 操作客户端.
     */
    public VirtualNetworkClient network() {
        return getInstance(VirtualNetworkClient.class);
    }

    /**
     * 获取块存储客户端.
     * <p> 仅限计算实例的存储.
     * @return 返回块存储客户端.
     */
    public BlockstorageClient blockStorage() {
        return getInstance(BlockstorageClient.class);
    }

    /**
     * 获取身份客户端.
     * <p> 用于访问身份相关 API.
     * @return 返回身份客户端.
     */
    public IdentityClient identity() {
        return getInstance(IdentityClient.class);
    }

    /**
     * 获取对象存储客户端.
     * <p> 不包括计算实例的存储.
     * @return 获取对象存储客户端.
     */
    public ObjectStorageClient objectStorage() {
        return getInstance(ObjectStorageClient.class);
    }

    /**
     * 获取实例.
     * @param type 实例类.
     * @param <T> 实例类型.
     * @return 返回对象.
     */
    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<?> type) {
        Objects.requireNonNull(type);
        if (!LAZY_LOADER_MAP.containsKey(type)) {
            throw new NoSuchElementException("No lazy loader of this type was found: " + type);
        }

        return (T) LAZY_LOADER_MAP.get(type).getInstance();
    }

    /**
     * 注册惰性加载器.
     * @param type 对象类.
     * @param supplier 对象提供器.
     * @param <T> 对象类型.
     */
    private <T> void registryLazyLoader(Class<? extends T> type, Supplier<T> supplier) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(supplier);
        LAZY_LOADER_MAP.put(type, new LazyLoader<>(supplier));
    }

}
