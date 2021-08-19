package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lamgc.oracle.sentry.ComputeInstanceManager;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author LamGC
 */
@SuppressWarnings("UnstableApiUsage")
public final class SshAuthIdentityProvider implements AutoCloseable {

    private final static String DEFAULT_AUTH_KEY = "@default";
    private final static Logger log = LoggerFactory.getLogger(SshAuthIdentityProvider.class);

    private final Map<String, SshAuthInfo> authInfoMap = new ConcurrentHashMap<>();
    private final ComputeInstanceManager instanceManager;
    private final File identityJsonFile;
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(SshAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .registerTypeAdapter(PublicKeyAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .registerTypeAdapter(PasswordAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .create();

    private final ScheduledExecutorService scheduledExec = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder()
                    .setNameFormat("Thread-SshInfoSave-%d")
                    .build());
    private final AtomicBoolean needSave = new AtomicBoolean(false);

    /**
     * 创建 SSH 认证配置提供器.
     * @param instanceManager 所属实例管理器.
     * @param identityJson 认证配置文件对象.
     */
    public SshAuthIdentityProvider(ComputeInstanceManager instanceManager, File identityJson) {
        this.instanceManager = instanceManager;
        this.identityJsonFile = identityJson;

        scheduledExec.scheduleAtFixedRate(() -> {
            if (!needSave.get()) {
                return;
            }
            needSave.set(false);
            try {
                SshAuthIdentityProvider.this.saveAuthInfo();
            } catch (Exception e) {
                log.warn("本次 SSH 认证配置保存失败.", e);
            }
        }, 60, 10, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "Thread-ProviderAutoSave-Close"));
    }

    /**
     * 添加 SSH 认证配置.
     * @param instanceId 配置对应的实例 Id.
     * @param authInfo SSH 认证配置对象.
     */
    public void addSshAuthIdentity(String instanceId, SshAuthInfo authInfo) {
        authInfoMap.put(instanceId, authInfo);
    }

    /**
     * 通过实例 Id 获取相应的实例 Id.
     * @param instanceId 实例 Id.
     * @return 返回指定实例 Id 的 SSH 认证配置.
     * @throws NoSuchElementException 当没有对应实例的配置时将抛出该异常.
     */
    public SshAuthInfo getAuthInfoByInstanceId(String instanceId) {
        if (!authInfoMap.containsKey(instanceId)) {
            SshAuthInfo defaultAuthInfo = getDefaultAuthInfo();
            if (defaultAuthInfo == null) {
                throw new NoSuchElementException("The SSH authentication information to which the " +
                        "specified instance ID belongs cannot be found: " + instanceId);
            }
            return defaultAuthInfo;
        }
        return authInfoMap.get(instanceId);
    }

    /**
     * 获取默认配置.
     * <p> 不建议使用, 因为服务器都不一样.
     * @return 如果有, 返回配置, 没有默认配置则返回 {@code null}.
     */
    public SshAuthInfo getDefaultAuthInfo() {
        if (authInfoMap.containsKey(DEFAULT_AUTH_KEY)) {
            return authInfoMap.get(DEFAULT_AUTH_KEY);
        }
        return null;
    }

    /**
     * 将所有认证配置保存到文件中.
     * @throws IOException 如果保存时发生异常, 则抛出.
     */
    private synchronized void saveAuthInfo() throws IOException {
        log.info("正在保存 SSH 认证配置...");
        String output = gson.toJson(authInfoMap);
        Files.writeString(identityJsonFile.toPath(), output,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        log.info("已成功保存 SSH 认证配置.");
    }

    /**
     * 通知 Provider 需要保存配置.
     */
    public void notifySaveInfo() {
        needSave.set(true);
    }

    /**
     * 从文件加载所有认证配置.
     * @throws IOException 如果读取文件时发生异常, 则抛出.
     */
    public synchronized void loadAuthInfo() throws IOException {
        if (!identityJsonFile.exists()) {
            log.warn("SSH 认证配置文件不存在, 跳过加载.");
            return;
        }
        Map<String, SshAuthInfo> map = gson.fromJson(new FileReader(identityJsonFile, StandardCharsets.UTF_8),
                new TypeToken<Map<String, SshAuthInfo>>(){}.getType());
        if (map == null) {
            log.warn("没有可用的 SSH 认证配置.");
            return;
        }

        for (String id : map.keySet()) {
            SshAuthInfo info = map.get(id);
            info.setProvider(this);
            addSshAuthIdentity(id, info);
        }

        Set<String> missingInstances = checkForMissingInstances();
        if (missingInstances.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (String missingInstanceId : missingInstances) {
            builder.append(missingInstanceId).append('\n');
        }
        log.warn("以下实例不存在对应的 SSH 认证配置:\n{}", builder);
    }

    /**
     * 获取所有不存在 SSH 配置的实例 Id.
     * @return 返回所有不存在对应 SSH 认证配置的实例 Id.
     */
    private Set<String> checkForMissingInstances() {
        if (instanceManager == null) {
            log.info("实例管理器未设置, 跳过检查.");
            return Collections.emptySet();
        }
        Set<String> instanceIdSet = instanceManager.getComputeInstances().stream()
                .map(ComputeInstance::getInstanceId)
                .collect(Collectors.toSet());
        for (String id : authInfoMap.keySet()) {
            instanceIdSet.remove(id);
        }
        return instanceIdSet;
    }

    @Override
    public void close() {
        scheduledExec.shutdown();
    }
}
